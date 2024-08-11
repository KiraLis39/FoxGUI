### 1) purge-local-repository
dependency:purge-local-repository
(не обязательно)

### 2) Build work jar
source:jar-no-fork install

### 3) fox-gui install-file
install:install-file -Dfile=H:\JavaProj\FoxLibrary\FoxGUI\target\fox-gui-6.2.82.jar -DgroupId=FoxLib39 -DartifactId=fox-gui -Dversion=6.2.82 -Dpackaging=jar -DcreateChecksum=true

### _Union build and register:_
2 => 3.
