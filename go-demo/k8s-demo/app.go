package main

import (
	"bufio"
	"context"
	"flag"
	"fmt"
	appsv1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/resource"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/kubernetes/scheme"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/tools/remotecommand"
	"k8s.io/client-go/util/homedir"
	"log"
	"os"
	"path/filepath"
)

/* https://juejin.cn/post/7092605076274937863 */
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

	scanner := bufio.NewScanner(os.Stdin)

	/* 创建 namespace */
	fmt.Println("create namespace or not (y/n)")
	scanner.Scan()
	if input := scanner.Text(); input == "y" {
		namespace := &corev1.Namespace{
			ObjectMeta: metav1.ObjectMeta{
				Name: "namespace-demo",
				Labels: map[string]string{
					"purpose": "demo",
				},
			},
		}
		_, err = client.CoreV1().Namespaces().Create(context.TODO(), namespace, metav1.CreateOptions{})
		if err != nil {
			log.Fatalf("Namespaces.Create error: %v", err)
		}
	}

	/* 查询 version */
	version, err := client.ServerVersion()
	if err != nil {
		log.Fatalf("ServerVersion error: %v", err)
	}
	log.Printf("ServerVersion: %v", version)

	/* 查询 deployment 列表 */
	deploymentList, err := client.AppsV1().Deployments("namespace-demo").List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		log.Fatalf("Deployments.List error: %v", err)
	}
	for index, deployment := range deploymentList.Items {
		log.Printf("Deployments.List.Items[%v]: %v, %v", index, deployment.Name, deployment.UID)
	}

	/* 查询 pod 列表 */
	podList, err := client.CoreV1().Pods("namespace-demo").List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		log.Fatalf("Pods.List error: %v", err)
	}
	for index, pod := range podList.Items {
		log.Printf("Pods.List.Items[%v]: %v", index, pod.Name)
	}

	/* 查询 service 列表 */
	serviceList, err := client.CoreV1().Services("namespace-demo").List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		log.Fatalf("Services.List error: %v", err)
	}
	for index, service := range serviceList.Items {
		log.Printf("Services.List.Items[%v]: %v", index, service.Name)
	}

	/* 新增 deployment */
	deployment := &appsv1.Deployment{
		ObjectMeta: metav1.ObjectMeta{
			Name:      "tomcat-demo",
			Namespace: "namespace-demo",
		},
		Spec: appsv1.DeploymentSpec{
			Replicas: int32Ptr(3),
			Selector: &metav1.LabelSelector{
				MatchLabels: map[string]string{
					"app": "tomcat-demo",
				},
			},
			Template: corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Labels: map[string]string{
						"app": "tomcat-demo",
					},
				},
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{
						{
							Name:  "tomcat-demo",
							Image: "tomcat:latest",
							Ports: []corev1.ContainerPort{
								{
									ContainerPort: 8080,
								},
							},
							Resources: corev1.ResourceRequirements{
								Requests: corev1.ResourceList{
									// 1 核等于 1000 豪核
									corev1.ResourceCPU:    resource.MustParse("500m"),
									corev1.ResourceMemory: resource.MustParse("256Mi"),
								},
								Limits: corev1.ResourceList{
									corev1.ResourceCPU:    resource.MustParse("1"),
									corev1.ResourceMemory: resource.MustParse("500Mi"),
								},
							},
						},
					},
				},
			},
		},
	}
	fmt.Println("create deployment or not (y/n)")
	scanner.Scan()
	if input := scanner.Text(); input == "y" {
		createdDeployment, err := client.AppsV1().Deployments("namespace-demo").Create(context.TODO(), deployment, metav1.CreateOptions{})
		if err != nil {
			log.Printf("Deployments.Create error: %v", err)
		}
		log.Printf("Deployments.Create: %v", createdDeployment.Name)
	}

	/* 删除 deployment */
	fmt.Println("delete deployment or not (y/n)")
	scanner.Scan()
	if input := scanner.Text(); input == "y" {
		deletePropagation := metav1.DeletePropagationForeground
		deleteOptions := metav1.DeleteOptions{
			PropagationPolicy: &deletePropagation,
		}
		err := client.AppsV1().Deployments("namespace-demo").Delete(context.TODO(), deployment.Name, deleteOptions)
		if err != nil {
			log.Printf("Deployments.Delete error: %v", err)
		}
	}

	/* 进入 pod 终端 */
	/* https://xuliangtang.github.io/posts/k8s-pod-shell/ */
	fmt.Println("stream with context or not (y/n)")
	scanner.Scan()
	if input := scanner.Text(); input == "y" {
		option := &corev1.PodExecOptions{
			Container: "tomcat-demo",
			// bash -c 表示后面要执行的是多个命令的脚本
			Command: []string{"bash", "-c", "stty -echo; exec bash"},
			Stdin:   true,
			Stdout:  true,
			Stderr:  true,
			TTY:     true,
		}

		req := client.CoreV1().RESTClient().Post().Resource("pods").
			Namespace("namespace-demo").
			Name(podList.Items[0].Name).
			SubResource("exec").
			VersionedParams(option, scheme.ParameterCodec)

		exec, err := remotecommand.NewSPDYExecutor(config, "POST", req.URL())
		if err != nil {
			log.Fatalf("NewSPDYExecutor error: %v", err)
		}

		err = exec.StreamWithContext(context.Background(), remotecommand.StreamOptions{
			Stdin:  os.Stdin,
			Stdout: os.Stdout,
			Stderr: os.Stderr,
			Tty:    true,
		})
		if err != nil {
			log.Fatalf("StreamWithContext error: %v", err)
		}
	}
}

func int32Ptr(i int32) *int32 {
	return &i
}
