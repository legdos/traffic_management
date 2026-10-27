param([int]$Port=8080,[string]$JavaHome=$env:JAVA_HOME,[switch]$Build)
$ErrorActionPreference='Stop';$root=Split-Path -Parent $PSScriptRoot;$backend=Join-Path $root 'backend'
$java=if($JavaHome){Join-Path $JavaHome 'bin\java.exe'}else{(Get-Command java.exe -ErrorAction Stop).Source};if(!(Test-Path $java)){throw '未找到 Java 17 或更高版本，请设置 JAVA_HOME 或使用 -JavaHome'}
$env:JAVA_HOME=if($JavaHome){$JavaHome}else{Split-Path (Split-Path $java -Parent) -Parent};$env:DB_URL=if($env:DB_URL){$env:DB_URL}else{'jdbc:mysql://127.0.0.1:3306/transport_groupwork?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true'};$env:DB_USERNAME=if($env:DB_USERNAME){$env:DB_USERNAME}else{'root'};$env:DB_PASSWORD=if($null -ne $env:DB_PASSWORD){$env:DB_PASSWORD}else{''}
if($Build){& (Join-Path $backend 'mvnw.cmd') '-DskipTests' 'package';if($LASTEXITCODE){throw '构建失败'}}
$targetJar=Join-Path $backend 'target\traffic_management-0.0.1-SNAPSHOT.jar'
$packagedJar=Join-Path $backend 'traffic_management-0.0.1-SNAPSHOT.jar'
$jar=if(Test-Path $targetJar){$targetJar}else{$packagedJar}
if(!(Test-Path $jar)){throw 'JAR 不存在，请先构建后端或恢复随包 JAR'}
Write-Host "Starting backend on http://127.0.0.1:$Port using $jar"
& $java -jar $jar "--server.port=$Port"
