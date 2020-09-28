一个简单的注解框架
主要的作用是
将项目中的所有的类全部按照接口方式实现 ,对外全部使用接口,解除每个类之间的耦合
具体的使用
1 module模块依赖
dependencies {
    .....
    implementation project(':apt')
    annotationProcessor project(':apt')
    .....
}
如上依赖并注解依赖 这样就能够使用Impl注解具体使用后面介绍,同时在
module的build.gradle中 添加如下
android {
    ...
    defaultConfig {
        ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [
                        bundleClassname: "com.wyz.apt.ApiImpls", // 代表生成的bundle类的路径
                        debug:"true" // 是否打印debug
                ]
            }
        }
    }
    ...
}

2 做完后就能使用了
创建一个接口和一个类 类实现该接口
public interface Hello {
    void hello() ;
}

@Impl(api = Hello.class)
public class HelloWorld implements Hello{
    String filed = "asdasd";
    String name = "asdasd";

    @Override
    public void hello() {
        Log.e("HelloWorld" , filed + name) ;
    }
}

点击小锤子进行编译 这样就出现了bundleClassname: "com.wyz.apt.ApiImpls" 该路径下的类 生成 并且在里边关联了Hello HelloWorld

3 如何使用
创建一个自定义类继承 ApiImpls 实现 ApiImplBundle 该接口是为了获取 ApiImpls父类中的内容
