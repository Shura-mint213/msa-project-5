FROM mcr.microsoft.com/dotnet/runtime:8.0
WORKDIR /app
COPY ./publish/ .

# Single-file executable
# имя exe файла после publish
ENTRYPOINT ["./exporter"]  