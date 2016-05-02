# kola-maven-plugin
Maven Plugin for the Kola Compiler

## Usage

Add the following to the `<plugins>` section in your pom.xml
```xml
	<plugin>
                <inherited>true</inherited>
                <groupId>se.sics.kola</groupId>
                <artifactId>kola-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                  <execution>
                    <goals>
                      <goal>kolac</goal>
                    </goals>
                    <phase>generate-sources</phase>
                  </execution>
                </executions>
        </plugin>
```
