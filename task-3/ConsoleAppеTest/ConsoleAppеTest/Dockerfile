FROM mcr.microsoft.com/dotnet/runtime:8.0 AS base
WORKDIR /app
COPY ./publish/ .

ENTRYPOINT ["dotnet", "Task3.dll"]