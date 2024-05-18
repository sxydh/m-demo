using Renci.SshNet;
using System.Text;

namespace SshDemo
{
    internal class Demo
    {
        static void Main(string[] args)
        {
            var host = "192.168.233.129";
            var username = "root";
            var password = "123";
            try
            {
                var connectionInfo = new ConnectionInfo(host, username, new PasswordAuthenticationMethod(username, password));
                using (var client = new SshClient(connectionInfo))
                {
                    Console.WriteLine($"Connecting to {host}...");
                    client.Connect();
                    Console.WriteLine($"Have connected to {host}");
                    var shellStream = client.CreateShellStream("", 0, 0, 0, 0, 1024);
                    Task.Run(() =>
                    {
                        using (var reader = new StreamReader(shellStream))
                        {
                            while (true)
                            {
                                var line = reader.ReadLine();
                                Console.WriteLine(line);
                            }
                        }
                    });
                    while (true)
                    {
                        var command = Console.ReadLine();
                        if (command != null)
                        {
                            shellStream.WriteLine(command);
                            shellStream.Flush();
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                Console.ReadKey();
            }
        }
    }
}
