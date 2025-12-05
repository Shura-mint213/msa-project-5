
Console.WriteLine("=== Batch Client Started ===");

var client = new HttpClient();

string url = "http://localhost:8080/api/job/run?source=csharp-client";

Console.WriteLine($"Sending POST request to: {url}");

try
{
    var response = await client.PostAsync(url, null);
    string content = await response.Content.ReadAsStringAsync();

    Console.WriteLine("Response status: " + response.StatusCode);
    Console.WriteLine("Response body: " + content);
}
catch (Exception ex)
{
    Console.WriteLine("Request failed:");
    Console.WriteLine(ex.ToString());
}

Console.WriteLine("=== Batch Client Finished ===");