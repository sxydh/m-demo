package main

import (
	"context"
	"flag"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
	"log"
	"path/filepath"
)

func main() {
	/* 构建配置 */
	var configFlag *string
	if home := homedir.HomeDir(); home != "" {
		configFlag = flag.String("kubeconfig", filepath.Join(home, ".kube", "config"), "")
	} else {
		configFlag = flag.String("kubeconfig", "C:/Users/Administrator/.kube/config", "")
	}
	flag.Parse()
	config, err := clientcmd.BuildConfigFromFlags("", *configFlag)
	if err != nil {
		log.Fatalf("BuildConfigFromFlags error: %v", err)
	}

	/* 构建客户端 */
	client, err := kubernetes.NewForConfig(config)
	if err != nil {
		log.Fatalf("NewForConfig error: %v", err)
	}

	/* 查询版本 */
	version, err := client.ServerVersion()
	if err != nil {
		log.Fatalf("ServerVersion error: %v", err)
	}
	log.Printf("ServerVersion: %v", version)

	/* 查询 pod 列表 */
	podList, err := client.CoreV1().Pods("default").List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		log.Fatalf("Pods.List error: %v", err)
	}
	for index, pod := range podList.Items {
		log.Printf("Pods.List.Items[%v]: %v", index, pod.Name)
	}

	/* 查询 service 列表 */
	serviceList, err := client.CoreV1().Services("default").List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		log.Fatalf("Services.List error: %v", err)
	}
	for index, service := range serviceList.Items {
		log.Printf("Services.List.Items[%v]: %v", index, service.Name)
	}
}
