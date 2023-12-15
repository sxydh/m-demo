using System;
using System.Collections.Generic;
using System.Net.NetworkInformation;
using System.Threading;

namespace IpDemo
{
    internal class Demo
    {
        static void Main(string[] args)
        {
            List<string> subnetIpList = GetSubnetIpList();
            List<string> aliveIpList = GetAliveIpList(subnetIpList);
            foreach (string ip in aliveIpList)
            {
                System.Console.WriteLine(ip);
            }
            Console.ReadLine();
        }

        static List<string> GetAliveIpList(List<string> ipList)
        {
            using (CountdownEvent countdownEvent = new CountdownEvent(ipList.Count))
            {
                List<string> aliveIpList = new List<string>();
                foreach (string ip in ipList)
                {
                    Ping ping = new Ping();
                    ping.PingCompleted += (sender, e) =>
                    {
                        if (e.Reply.Status == IPStatus.Success)
                        {
                            aliveIpList.Add((string)e.UserState);
                        }
                        ping.Dispose();
                        countdownEvent.Signal();
                    };
                    ping.SendAsync(ip, 5000, ip);
                }
                countdownEvent.Wait();
                return aliveIpList;
            }
        }

        static List<string> GetSubnetIpList()
        {
            // 获取子网掩码和网关
            string subnetMask = null;
            string gateway = null;
            NetworkInterface[] networkInterfaces = NetworkInterface.GetAllNetworkInterfaces();
            foreach (NetworkInterface networkInterface in networkInterfaces)
            {
                if (subnetMask != null && gateway != null)
                {
                    break;
                }
                if (networkInterface.OperationalStatus == OperationalStatus.Up)
                {
                    // 获取子网掩码
                    if (subnetMask == null)
                    {
                        UnicastIPAddressInformationCollection unicastIPAddressInformationCollection = networkInterface.GetIPProperties().UnicastAddresses;
                        foreach (UnicastIPAddressInformation unicastIPAddressInformation in unicastIPAddressInformationCollection)
                        {
                            if (unicastIPAddressInformation.Address.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork)
                            {
                                subnetMask = unicastIPAddressInformation.IPv4Mask.ToString();
                                break;
                            }
                        }
                    }

                    // 获取网关
                    if (gateway == null)
                    {
                        GatewayIPAddressInformationCollection gatewayIPAddressInformationCollection = networkInterface.GetIPProperties().GatewayAddresses;
                        foreach (GatewayIPAddressInformation gatewayIPAddressInformation in gatewayIPAddressInformationCollection)
                        {
                            if (gatewayIPAddressInformation.Address.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork)
                            {
                                gateway = gatewayIPAddressInformation.Address.ToString();
                                break;
                            }
                        }
                    }
                }
            }
            List<string> subnetIpList = new List<string>();
            if (subnetMask == null || gateway == null)
            {
                return subnetIpList;
            }

            // 根据子网掩码和网关计算子网
            string[] subnetMaskArray = subnetMask.Split('.');
            string[] gatewayArray = gateway.Split('.');
            string[] subnetArray = new string[4];
            for (int i = 0; i < 4; i++)
            {
                subnetArray[i] = (int.Parse(subnetMaskArray[i]) & int.Parse(gatewayArray[i])).ToString();
            }

            // 根据子网计算子网IP列表
            for (int i = 1; i < 255; i++)
            {
                subnetIpList.Add(subnetArray[0] + "." + subnetArray[1] + "." + subnetArray[2] + "." + i);
            }

            return subnetIpList;
        }

    }

}
