from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python import PythonOperator, BranchPythonOperator
from airflow.operators.email import EmailOperator
from airflow.utils.trigger_rule import TriggerRule
import random
import pandas as pd

default_args = {
    'owner': 'you',
    'depends_on_past': False,
    'retries': 3,                
    'retry_delay': timedelta(minutes=1),
    'email_on_failure': False,
    'email_on_success': False,
}

dag = DAG(
    'task1_demo_pipeline',
    default_args=default_args,
    description='PoC для Задания 1',
    schedule=None,           # запускаем вручную
    start_date=datetime(2025, 1, 1),
    catchup=False,
    tags=['task1', 'poc'],
)

def read_data():
    # Имитируем чтение из CSV
    df = pd.DataFrame({'order_id': range(1, 11), 'amount': [random.randint(100, 10000) for _ in range(10)]})
    df.to_csv('/tmp/orders.csv', index=False)
    print("Данные прочитаны и сохранены в /tmp/orders.csv")

def check_condition(**context):
    df = pd.read_csv('/tmp/orders.csv')
    total = df['amount'].sum()
    if total > 30000:
        return 'high_volume_branch'
    else:
        return 'normal_volume_branch'

def high_volume():
    print("Объём высокий — запускаем тяжёлую обработку")

def normal_volume():
    print("Объём нормальный")

def fail_task():
    raise ValueError("Имитация ошибки для демонстрации retry и email")

read_task = PythonOperator(task_id='read_data', python_callable=read_data, dag=dag)

branch_task = BranchPythonOperator(
    task_id='check_volume_branch',
    python_callable=check_condition,
    dag=dag,
)

high_task = PythonOperator(task_id='high_volume_branch', python_callable=high_volume, dag=dag)
normal_task = PythonOperator(task_id='normal_volume_branch', python_callable=normal_volume, dag=dag)

# Задача, которая падает — чтобы показать retry + email
fail_demo_task = PythonOperator(
    task_id='fail_demo',
    python_callable=fail_task,
    retries=2,  
    retry_delay=timedelta(seconds=10),
    dag=dag,
)

# Уведомления
email_success = EmailOperator(
    task_id='email_success',
    to='test@bk.ru', 
    subject='Пайплайн успешно завершён',
    html_content='Пайплайн task1_demo_pipeline завершился успешно!',
    trigger_rule=TriggerRule.ALL_SUCCESS,
    dag=dag,
)

email_failure = EmailOperator(
    task_id='email_failure',
    to='test@bk.ru',
    subject='Пайплайн УПАЛ!',
    html_content='Пайплайн task1_demo_pipeline завершился с ошибкой!',
    trigger_rule=TriggerRule.ONE_FAILED,
    dag=dag,
)

# Зависимости
read_task >> branch_task
branch_task >> [high_task, normal_task]
high_task >> [email_success, email_failure]
normal_task >> [email_success, email_failure]
fail_demo_task >> [email_success, email_failure]