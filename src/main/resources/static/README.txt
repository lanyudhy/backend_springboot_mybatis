-- Druid监控页面
http://localhost:8080/druid/index.html
-- Actuator监控页面
eg.: http://localhost:8080/metrics

-- 将本地 jar 安装到本地的Maven仓库里面
mvn install:install-file -Dfile=D:/work/Plato/NLP/target/plato-nlp-0.9.1.jar -DgroupId=org.plato.nlp -DartifactId=plato-nlp -Dversion=0.9.1 -Dpackaging=jar

-- plato-nlp 作为 plato 的依赖工程需要做下面几步操作：
-- 1. 为了 plato-nlp 工程能够通过编译，需删除 src/test/resources 下面的所有文件，但是该空文件夹需要保留。改好后重新打包然后 install 到 repository
-- 2. 需要把 plato-nlp 工程的  src/main/resources 下面的所有文件及文件夹拷贝到 plato 工程
-- 3. 同样地， plato-nlp 工程根目录下的  workspace 文件夹也要拷贝到 plato 工程的根目录下（其实也可以放到src/main/resources，只不过程序里的引用路径要改一下） 