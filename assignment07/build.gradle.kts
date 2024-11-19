dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.mybatis:mybatis:3.5.16")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3")


    runtimeOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.postgresql:postgresql")
}
