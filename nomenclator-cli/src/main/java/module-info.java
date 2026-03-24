module nomenclator.cli {
    opens uk.co.varia.nomenclator.cli to info.picocli;

    requires info.picocli;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires jakarta.annotation;
}
