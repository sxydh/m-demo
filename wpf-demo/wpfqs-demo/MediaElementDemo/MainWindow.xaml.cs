using System;
using System.IO;
using System.Net.Http;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;

namespace MediaElementDemo
{
    /// <summary>
    /// MainWindow.xaml 的交互逻辑
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();

            mediaElement.Source = new Uri("C:\\Users\\Administrator\\Desktop\\1.mp4");
            mediaElement.LoadedBehavior = MediaState.Manual;
            mediaElement.Loaded += new RoutedEventHandler((sender, e) => { 
                (sender as MediaElement).Play();
            });
        }
    }
}
