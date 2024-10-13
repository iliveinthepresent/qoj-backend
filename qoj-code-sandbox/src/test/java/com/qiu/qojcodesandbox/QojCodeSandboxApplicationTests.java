package com.qiu.qojcodesandbox;

import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.jaxrs.JerseyDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class QojCodeSandboxApplicationTests {

    @Test
    void contextLoads() throws InterruptedException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);


        String image = "openjdk:8-alpine";

        String[] inputArgsArray = {"1 3"};

        String[] cmdArray = ArrayUtil.append(new String[]{"/bin/sh", "-c", "java", "-cp", "/app", "Main"}, inputArgsArray);
//        String[] cmdArray = new String[]{"/bin/sh", "-c", "java -cp /app Main 1 3"};

//        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
//        HostConfig hostConfig = new HostConfig();
//        hostConfig.withMemory(100 * 1000 * 1000L);
//        hostConfig.withMemorySwap(0L);
//        hostConfig.withCpuCount(1L);
////        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
//        hostConfig.setBinds(new Bind("/home/qiuqiu/Projects/qoj/qoj-code-sandbox/tmpCode/1d8d75a6-a334-4001-ba86-9fd9b50d0422", new Volume("/app")));
//        CreateContainerResponse createContainerResponse = containerCmd
//                .withHostConfig(hostConfig)
//                .withNetworkDisabled(true)
//                .withReadonlyRootfs(true)
//                .withAttachStdin(true)
//                .withAttachStderr(true)
//                .withAttachStdout(true)
//                .withTty(true)
//                .exec();
//        System.out.println(createContainerResponse);
        String containerId = "1e88cf4e7311b97abefeadb863f3c99add2e150f78efba75453eae50d43754b8";
        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
        if (!containerInfo.getState().getRunning()) {
            // 仅当容器未运行时才启动
            dockerClient.startContainerCmd(containerId).exec();
        } else {
            System.out.println("Container is already running.");
        }


        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(cmdArray)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();

        String execCreateCmdResponseId = execCreateCmdResponse.getId();


        ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
            @Override
            public void onComplete() {
                // 如果执行完成，则表示没超时
                super.onComplete();
            }

            @Override
            public void onNext(Frame frame) {
                StreamType streamType = frame.getStreamType();
                if (StreamType.STDERR.equals(streamType)) {

                    System.out.println("输出错误结果：" + frame.getPayload());
                } else {
                    System.out.println("输出结果：" + frame.getPayload());
                }
                super.onNext(frame);
            }
        };
        String execId = execCreateCmdResponse.getId();

        dockerClient.execStartCmd(execId)
                .exec(execStartResultCallback)
                .awaitCompletion(90000, TimeUnit.MILLISECONDS);
    }


    @Test
    public void  yes() throws InterruptedException, IOException {


            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("unix:///var/run/docker.sock")
                    .build();

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .build();

            DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

            String containerId = "1e88cf4e7311b97abefeadb863f3c99add2e150f78efba75453eae50d43754b8";

            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
            if (!containerInfo.getState().getRunning()) {
                dockerClient.startContainerCmd(containerId).exec();
            } else {
                System.out.println("Container is already running.");
            }

            // 创建 ExecCreateCmdResponse 来启动 Java 程序
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("/bin/sh", "-c", "java -cp /app Main")
                    .withAttachStdin(true)  // 允许标准输入
                    .withAttachStdout(true) // 允许标准输出
                    .withAttachStderr(true) // 允许标准错误输出
                    .exec();

            String execId = execCreateCmdResponse.getId();

            // 创建 PipedInputStream 和 PipedOutputStream 来模拟输入
            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

            // 启动 ExecStartCmd，使用 PipedInputStream 作为标准输入
            dockerClient.execStartCmd(execId)
                    .withStdIn(pipedInputStream)
                    .withDetach(false) // 保持与容器的连接
                    .withTty(false)
                    .exec(new ExecStartResultCallback(System.out, System.err));

            // 通过 PipedOutputStream 模拟输入
            new Thread(() -> {
                try {
                    // 向程序输入 1 和 3
                    pipedOutputStream.write("1\n".getBytes());
//                    pipedOutputStream.flush();
                    TimeUnit.SECONDS.sleep(1);  // 等待一段时间，模拟用户输入的延时

                    pipedOutputStream.write("99\n".getBytes());
                    pipedOutputStream.flush();
                    pipedOutputStream.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            // 等待执行完成
            new ExecStartResultCallback(System.out, System.err)
                    .awaitCompletion(90, TimeUnit.SECONDS);




    }

    @Test
    public void newTry() throws InterruptedException, IOException {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("unix:///var/run/docker.sock")
                    .build();

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .sslConfig(config.getSSLConfig())
                    .build();

            DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        String image = "openjdk:8-alpine";

//        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
//        HostConfig hostConfig = new HostConfig();
//        hostConfig.withMemory(100 * 1000 * 1000L);
//        hostConfig.withMemorySwap(0L);
//        hostConfig.withCpuCount(1L);
////        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
//        hostConfig.setBinds(new Bind("/home/qiuqiu/Projects/qoj/qoj-code-sandbox/tmpCode/1d8d75a6-a334-4001-ba86-9fd9b50d0422", new Volume("/app")));
//        CreateContainerResponse createContainerResponse = containerCmd
//                .withHostConfig(hostConfig)
//                .withNetworkDisabled(true)
//                .withReadonlyRootfs(true)
//                .withAttachStdin(true)
//                .withAttachStderr(true)
//                .withAttachStdout(true)
//                .withTty(true)
//                .exec();


            String containerId = "2f1306aa28cf87cf432b6f54c76b930b9e59d602cee41d631d7cd687904269d6";

            InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
            if (!containerInfo.getState().getRunning()) {
                dockerClient.startContainerCmd(containerId).exec();
            } else {
                System.out.println("Container is already running.");
            }

            // 创建 ExecCreateCmdResponse 来启动 Java 程序
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd("/bin/sh", "-c", "java -cp /app Main")
                    .withAttachStdin(true)  // 允许标准输入
                    .withAttachStdout(true) // 允许标准输出
                    .withAttachStderr(true) // 允许标准错误输出
                    .exec();

            String execId = execCreateCmdResponse.getId();

            // 创建 PipedInputStream 和 PipedOutputStream 来模拟输入
            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

            // 启动 ExecStartCmd，使用 PipedInputStream 作为标准输入
            dockerClient.execStartCmd(execId)
                    .withStdIn(pipedInputStream)
                    .withDetach(false) // 保持与容器的连接
                    .withTty(false)
                    .exec(new ExecStartResultCallback(System.out, System.err));

            // 通过 PipedOutputStream 模拟输入
//            new Thread(() -> {
                try {
                    // 向程序输入 1 和 3
                    pipedOutputStream.write("1\n".getBytes());
                    pipedOutputStream.flush();
                    TimeUnit.SECONDS.sleep(1);  // 等待一段时间，模拟用户输入的延时

                    pipedOutputStream.write("3\n".getBytes());
                    pipedOutputStream.flush();

                    // 输入完成后，关闭输出流，表示输入结束
                    pipedOutputStream.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
//            }).start();

            // 等待执行完成
//            new ExecStartResultCallback(System.out, System.err)
//                    .awaitCompletion(90, TimeUnit.SECONDS);

            dockerClient.close();

    }


    @Test
    public void compileAndRunJavaInDocker() throws InterruptedException, IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        String containerId = "2f1306aa28cf87cf432b6f54c76b930b9e59d602cee41d631d7cd687904269d6";

        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
        if (!containerInfo.getState().getRunning()) {
            dockerClient.startContainerCmd(containerId).exec();
        } else {
            System.out.println("Container is already running.");
        }

        // Step 1: 执行 Java 编译命令 (javac)
        ExecCreateCmdResponse compileCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/bin/sh", "-c", "javac /app/Main.java") // 编译 Java 文件
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        String compileExecId = compileCmdResponse.getId();

        // 启动编译命令，输出到命令行
        dockerClient.execStartCmd(compileExecId)
                .withDetach(false)
                .withTty(false)
                .exec(new ExecStartResultCallback(System.out, System.err));

        // Step 2: 执行编译后的 Java 程序
        ExecCreateCmdResponse runCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/bin/sh", "-c", "java -cp /app Main")
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        String runExecId = runCmdResponse.getId();

        // 创建 PipedInputStream 和 PipedOutputStream 来模拟用户输入
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

        // 启动 Java 程序，输出到命令行
        dockerClient.execStartCmd(runExecId)
                .withStdIn(pipedInputStream)
                .withDetach(false)
                .withTty(false)
                .exec(new ExecStartResultCallback(System.out, System.err));

        // 模拟输入 1 和 3
        try {
            pipedOutputStream.write("1\n".getBytes());
            pipedOutputStream.flush();
            TimeUnit.SECONDS.sleep(1);  // 等待一段时间，模拟用户输入的延时

            pipedOutputStream.write("3\n".getBytes());
            pipedOutputStream.flush();

            // 输入完成后，关闭输出流，表示输入结束
            pipedOutputStream.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        dockerClient.close();
    }


    @Test
    public void compileAndRunJavaInDockerq() throws InterruptedException, IOException {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        String image = "openjdk:8-alpine";

//        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
//        HostConfig hostConfig = new HostConfig();
//        hostConfig.withMemory(100 * 1000 * 1000L);
//        hostConfig.withMemorySwap(0L);
//        hostConfig.withCpuCount(1L);
////        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理配置字符串"));
//        hostConfig.setBinds(new Bind("/home/qiuqiu/Projects/qoj/qoj-code-sandbox/tmpCode/1d8d75a6-a334-4001-ba86-9fd9b50d0422", new Volume("/app")));
//        CreateContainerResponse createContainerResponse = containerCmd
//                .withHostConfig(hostConfig)
//                .withNetworkDisabled(true)
//                .withReadonlyRootfs(true)
//                .withAttachStdin(true)
//                .withAttachStderr(true)
//                .withAttachStdout(true)
//                .withTty(true)
//                .exec();
        String containerId = "2f1306aa28cf87cf432b6f54c76b930b9e59d602cee41d631d7cd687904269d6";

        InspectImageResponse inspectImageResponse = dockerClient.inspectImageCmd(image).exec();

        InspectContainerResponse containerInfo = dockerClient.inspectContainerCmd(containerId).exec();
        if (!containerInfo.getState().getRunning()) {
            dockerClient.startContainerCmd(containerId).exec();
        } else {
            System.out.println("Container is already running.");
        }

        // Step 1: 执行 Java 编译命令 (javac)
        ExecCreateCmdResponse compileCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/bin/sh", "-c", "javac /app/Main.java") // 编译 Java 文件
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        String compileExecId = compileCmdResponse.getId();

        // 启动编译命令并捕获输出
        ByteArrayOutputStream compileOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream compileErrorStream = new ByteArrayOutputStream();

// 捕获输出到 ByteArrayOutputStream
        ExecStartResultCallback compileResultCallback = new ExecStartResultCallback(compileOutputStream, compileErrorStream);

// 启动命令
        dockerClient.execStartCmd(compileExecId)
                .withDetach(false)
                .withTty(false)
                .exec(compileResultCallback);

// 等待命令执行完成
        compileResultCallback.awaitCompletion(9, TimeUnit.SECONDS);

// 确保输出流已关闭并刷新
        compileOutputStream.flush();
        compileErrorStream.flush();

// 获取输出和错误信息
        String compileOutput = compileOutputStream.toString();
        String compileErrors = compileErrorStream.toString();



        // 检查编译是否成功

        if (!compileErrors.isEmpty()) {
            System.out.println("Compilation errors:");
            System.out.println(compileErrors);
            return; // 如果编译失败，停止执行后续步骤
        }

        System.out.println("Compilation successful.");
        System.out.println("Compiler output:");
        System.out.println(compileOutputStream.toString());

        // Step 2: 执行编译后的 Java 程序



        ExecCreateCmdResponse runCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd("/bin/sh", "-c", "java -cp /app Main")
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        String runExecId = runCmdResponse.getId();

        // 创建 PipedInputStream 和 PipedOutputStream 来模拟用户输入
        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

        // 启动 Java 程序

        ByteArrayOutputStream runOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream runErrorStream = new ByteArrayOutputStream();
        ExecStartResultCallback runResultCallback = new ExecStartResultCallback(runOutputStream, runErrorStream);


        dockerClient.execStartCmd(runExecId)
                .withStdIn(pipedInputStream)
                .withDetach(false)
                .withTty(false)
                .exec(runResultCallback);

        // 模拟输入 1 和 3
        try {
            pipedOutputStream.write("1 3\n".getBytes());
            pipedOutputStream.flush();

            // 输入完成后，关闭输出流，表示输入结束
            pipedOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        runResultCallback.awaitCompletion(3, TimeUnit.SECONDS);
        runOutputStream.flush();
        runErrorStream.flush();
        // 输出执行的结果
        System.out.println("Program output:");
        System.out.println(runOutputStream.toString());

        // 输出运行时错误
        String runErrors = runErrorStream.toString();
        if (!runErrors.isEmpty()) {
            System.out.println("Runtime errors:");
            System.out.println(runErrors);
        }

        dockerClient.close();
    }



}
