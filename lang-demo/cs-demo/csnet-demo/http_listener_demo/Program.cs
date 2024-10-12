using System;
using System.IO;
using System.Net;
using System.Threading.Tasks;

namespace http_listener_demo
{
    internal class Program
    {

        private static readonly string baseUrl = "http://localhost:5000/";

        static void Main()
        {
            string prefix = "http://localhost:8080/";
            string rootDirectory = @"D:\Code\1-My\m-demo\lang-demo\cs-demo\csnet-demo\http_listener_demo"; // 修改为你的静态文件路径

            SimpleHttpServer server = new SimpleHttpServer(prefix, rootDirectory);
            server.Start();

            server.Stop();

            Task.Delay(500000).Wait();
        }

    }

    class SimpleHttpServer
    {
        private readonly HttpListener _listener;
        private readonly string _rootDirectory = Path.Combine(Directory.GetCurrentDirectory(), "ROOT");

        public SimpleHttpServer(int port)
        {
            _listener = new HttpListener();
            _listener.Prefixes.Add($"http://localhost:{port}");
        }

        public Task Start()
        {
            _listener.Start();
            return Task.Run(() =>
            {
                while (_listener.IsListening)
                {
                    var context = _listener.GetContext();
                    ProcessRequest(context);
                }
            });
        }

        public void Stop()
        {
            _listener.Stop();
        }

        private void ProcessRequest(HttpListenerContext context)
        {
            string requestPath = context.Request.Url.AbsolutePath.TrimStart('/');
            string targetPath = Path.Combine(_rootDirectory, requestPath);

            if (string.IsNullOrWhiteSpace(requestPath) || Directory.Exists(targetPath))
            {
                ProcessDirectory(context, targetPath);
            }
            else if (File.Exists(targetPath))
            {
                ProcessFile(context, targetPath);
            }
            else
            {
                context.Response.StatusCode = (int)HttpStatusCode.NotFound;
                context.Response.Close();
            }
        }

        private void ProcessDirectory(HttpListenerContext context, string directoryPath)
        {
            try
            {
                var subDirectories = Directory.GetDirectories(directoryPath);
                var subFiles = Directory.GetFiles(directoryPath);

                using (var writer = new StreamWriter(context.Response.OutputStream))
                {
                    writer.WriteLine("<html><body><ul>");
                    foreach (var subDir in subDirectories)
                    {
                        var subDirName = Path.GetFileName(subDir);
                        writer.WriteLine($"<li><a href=\"{subDirName}/\">{subDirName}/</a></li>");
                    }
                    foreach (var subFile in subFiles)
                    {
                        var subFileName = Path.GetFileName(subFile);
                        writer.WriteLine($"<li><a href=\"{subFileName}\">{subFileName}</a></li>");
                    }
                    writer.WriteLine("</ul></body></html>");
                }
                context.Response.ContentType = "text/html";
                context.Response.StatusCode = (int)HttpStatusCode.OK;
            }
            catch (Exception)
            {
                context.Response.StatusCode = (int)HttpStatusCode.InternalServerError;
            }
            finally
            {
                context.Response.Close();
            }
        }

        private void ProcessFile(HttpListenerContext context, string filePath)
        {
            try
            {
                context.Response.ContentType = GetContentType(filePath);
                using (var fileStream = File.OpenRead(filePath))
                {
                    fileStream.CopyTo(context.Response.OutputStream);
                }
                context.Response.StatusCode = (int)HttpStatusCode.OK;
            }
            catch (Exception)
            {
                context.Response.StatusCode = (int)HttpStatusCode.InternalServerError;
            }
            finally
            {
                context.Response.Close();
            }
        }

        private string GetContentType(string path)
        {
            string extension = Path.GetExtension(path).ToLower();
            switch (extension)
            {
                case ".html": return "text/html";
                case ".css": return "text/css";
                case ".js": return "application/javascript";
                case ".txt": return "text/plain";
                case ".png": return "image/png";
                case ".jpg":
                case ".jpeg": return "image/jpeg";
                case ".gif": return "image/gif";
                default: return "application/octet-stream";
            }
        }

    }

}
