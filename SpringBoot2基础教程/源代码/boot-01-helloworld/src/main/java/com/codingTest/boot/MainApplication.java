package com.codingTest.boot;

import ch.qos.logback.core.db.DBHelper;
import com.codingTest.boot.bean.Pet;
import com.codingTest.boot.bean.User;
import com.codingTest.boot.config.MyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 主程序类
 * @SpringBootApplication: 这是一个SpringBoot应用
 * scanBasePackages: 指定扫描的包路径
 */
@SpringBootApplication
public class MainApplication {
    public static void main(String[] args) {
        // 1. 返回 IOC 容器
        ConfigurableApplicationContext run = SpringApplication.run(MainApplication.class, args);

        // 2. 查看容器里面的组件
        String[] beanDefinitionNames = run.getBeanDefinitionNames();
        for(String name: beanDefinitionNames){
            System.out.println(name);
        }

        // 3. 从容器中获取组件
        Pet tom01 = run.getBean("tom", Pet.class);
        Pet tom02 = run.getBean("tom", Pet.class);
        System.out.println("组件：" + (tom01 == tom02));

        // 4. MyConfig$$EnhancerBySpringCGLIB
        MyConfig bean = run.getBean(MyConfig.class);
        System.out.println(bean);

        // 如果@Configuration(proxyBeanMethods = true)代理对象调用方法。SpringBoot总会检查这个组件在容器中有没有。
        // 保持组件单实例
        User user = bean.user01();
        User user1 = bean.user01();
        System.out.println(user == user1);

        User user01 = run.getBean("user01", User.class);
        Pet tom = run.getBean("tom", Pet.class);
        System.out.println("用户的宠物" + (user01.getPet() == tom));

        // 5. 获取组件
        String[] beanNamesForType = run.getBeanNamesForType(User.class);
        System.out.println("-------------------");
        for (String s : beanNamesForType) {
            System.out.println(s);
        }

        DBHelper bean1 = run.getBean(DBHelper.class);
        System.out.println(bean1);
    }
}
