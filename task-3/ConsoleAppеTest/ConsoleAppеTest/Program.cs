using System.Data;
using Npgsql;

// --- Main logic ---
Console.WriteLine("Запуск экспорта данных из PostgreSQL...");

var connString = $"Host={GetEnv("DB_HOST", "postgres")};" +
                 $"Port=5432;" +
                 $"Username={GetEnv("DB_USER", "airflow")};" +
                 $"Password={GetEnv("DB_PASS", "airflow")};" +
                 $"Database={GetEnv("DB_NAME", "airflow")};";

try
{
    await using var conn = new NpgsqlConnection(connString);
    await conn.OpenAsync();

    await using var cmd = new NpgsqlCommand("SELECT schemaname, tablename, tableowner FROM pg_tables LIMIT 20", conn);
    await using var reader = await cmd.ExecuteReaderAsync();

    var dt = new DataTable();
    dt.Load(reader);

    var filename = $"/tmp/export_{DateTime.Now:yyyyMMdd_HHmmss}.csv";
    CsvExtensions.WriteToCsv(dt, filename); // вызов через статический класс

    Console.WriteLine($"УСПЕШНО: Экспортировано {dt.Rows.Count} строк в {filename}");
    File.WriteAllText("/tmp/SUCCESS", "export completed");
}
catch (Exception ex)
{
    Console.WriteLine($"ОШИБКА: {ex.Message}");
    Environment.Exit(1);
}

// --- Helper methods ---
static string GetEnv(string name, string defaultValue)
    => Environment.GetEnvironmentVariable(name) ?? defaultValue;

// --- Extension method must be in a top-level static class ---
public static class CsvExtensions
{
    public static void WriteToCsv(this DataTable dt, string path)
    {
        using var writer = new StreamWriter(path);
        // Заголовки
        for (int i = 0; i < dt.Columns.Count; i++)
        {
            writer.Write(EscapeCsvField(dt.Columns[i].ColumnName));
            if (i < dt.Columns.Count - 1) writer.Write(",");
        }
        writer.WriteLine();

        // Строки
        foreach (DataRow row in dt.Rows)
        {
            for (int i = 0; i < dt.Columns.Count; i++)
            {
                var value = row[i]?.ToString() ?? "";
                writer.Write(EscapeCsvField(value));
                if (i < dt.Columns.Count - 1) writer.Write(",");
            }
            writer.WriteLine();
        }
    }

    private static string EscapeCsvField(string field)
    {
        if (field == null) return "";
        // Оборачиваем в кавычки и экранируем внутренние кавычки
        return $"\"{field.Replace("\"", "\"\"")}\"";
    }
}