# PCBuilder — Projeto Final

Aplicativo Android acadêmico para selecionar componentes de computador, verificar compatibilidade, gerar montagens por orçamento e comparar cotações reais.

**PCBuilder 1.0 — Projeto Final.** O aplicativo usa por padrão a API pública https://pcbuilder-api-dc2r.onrender.com. Para compilar e avaliar o Android com essa API, não é necessário instalar Node.js, executar um servidor local ou informar chaves de API.

[Repositório](https://github.com/victorgonzalez-prog/PCBuilder) · [Versões para download](https://github.com/victorgonzalez-prog/PCBuilder/releases) · [Página da API](https://pcbuilder-api-dc2r.onrender.com) · [Status do serviço](https://pcbuilder-api-dc2r.onrender.com/api/health)

O endereço inicial da API apresenta o status do serviço e links para as consultas públicas. As rotas /api/health, /api/catalogo, /api/precos e /api/status retornam dados em JSON.

## Versões e pré-requisitos

As versões do Gradle e dos plugins pertencem ao projeto. As dependências Android são baixadas automaticamente durante a sincronização; não é necessário instalá-las manualmente.

| Item | Versão/configuração desta entrega | Onde conferir |
| --- | --- | --- |
| Android Studio | Narwhal Feature Drop 2025.1.2 ou uma versão posterior que suporte AGP 8.12 | [Compatibilidade oficial](https://developer.android.com/studio/releases#android_gradle_plugin_and_android_studio_compatibility) |
| JDK para executar o Gradle | **21**; validação realizada com 21.0.10 | Gradle JDK na IDE; JAVA_HOME no terminal |
| Gradle | **8.13**, fornecido pelo Wrapper | gradle/wrapper/gradle-wrapper.properties |
| Android Gradle Plugin | **8.12.3** | gradle/libs.versions.toml |
| Kotlin e plugins Compose/Serialization | **2.3.20** | gradle/libs.versions.toml |
| compileSdk / targetSdk | **36 / 36** | app/build.gradle.kts |
| SDK Build Tools | **35.0.0**, padrão do AGP 8.12 | SDK Manager e [documentação do AGP](https://developer.android.com/build/releases/agp-8-12-0-release-notes#compatibility) |
| SDK Platform-Tools / adb | Validação com **36.0.0 / 1.0.41**; instalar pelo SDK Manager | SDK Manager |
| Dispositivo mínimo | Android **7.0 / API 24** | minSdk em app/build.gradle.kts |
| Dispositivo usado nos testes | Emulador Android **16 / API 36**, Medium Phone | Device Manager |
| Alvo do bytecode Java/Kotlin | **11**; isso não significa executar o Gradle com JDK 11 | compileOptions e compilerOptions |
| Compose BOM | **2026.01.00**; define as versões de UI, Foundation e Material 3 | app/build.gradle.kts |
| Navigation 3 runtime/UI | **1.2.1** | app/build.gradle.kts |
| Lifecycle ViewModel Compose/KTX | **2.9.4** | app/build.gradle.kts |
| Activity Compose | **1.13.0** | app/build.gradle.kts |
| kotlinx.serialization JSON | **1.11.0** | app/build.gradle.kts |
| JUnit local | **4.13.2** | gradle/libs.versions.toml |
| AndroidX JUnit / Espresso | **1.1.5 / 3.5.1** | gradle/libs.versions.toml |
| Node.js, somente para o backend opcional | Validação com **22.22.2**; engines exige 20 ou superior | backend/package.json |
| npm, somente para o backend opcional | Validação com **10.9.7** | Usar npm ci e backend/package-lock.json |
| Express / dotenv, backend opcional | **4.22.3 / 16.6.1** resolvidos no lockfile | backend/package-lock.json |
| Git, opcional para clonar | Validação com **2.51.0** no Windows; o download ZIP dispensa Git | git --version |

A saída de gradlew --version mostra Kotlin 2.0.21, usado internamente pelo próprio Gradle. O plugin Kotlin do aplicativo é 2.3.20, conforme o catálogo de versões; esses números têm funções diferentes.

O catálogo também declara Core KTX 1.10.1, Material Components 1.10.0 e Activity 1.8.0. O Gradle pode resolver versões transitivas superiores por exigência das outras bibliotecas. Os arquivos app/build.gradle.kts e gradle/libs.versions.toml são a referência das declarações; o comando de dependências abaixo mostra a árvore efetivamente resolvida.

```text
:app:dependencies --configuration debugRuntimeClasspath
```

Execute essa tarefa com o Wrapper: .\gradlew.bat no Windows ou ./gradlew no macOS/Linux. Não instale um Gradle global nem aceite atualizações de AGP/Kotlin para conseguir abrir esta entrega.

É necessária conexão com a internet na primeira compilação para baixar o Gradle e as bibliotecas dos repositórios oficiais Google, Maven Central e Gradle Plugin Portal. O aplicativo também precisa de internet para consultar preços. Emulador, imagem de sistema e virtualização são necessários somente se não for utilizado um aparelho físico. Não há código nativo próprio que exija instalar NDK ou CMake para esta entrega.

## Abrir no Android Studio

### Obter o projeto

```text
git clone https://github.com/victorgonzalez-prog/PCBuilder.git
cd PCBuilder
```

Também é possível usar Code → Download ZIP no GitHub e extrair todos os arquivos antes de abrir. Abra a raiz extraída por File → Open e aguarde a importação como projeto Gradle. Não execute diretamente de dentro do ZIP.

### Selecionar o JDK 21

1. No Windows/Linux, abra File → Settings. No macOS, abra Android Studio → Settings ou Preferences.
2. Acesse Build, Execution, Deployment → Build Tools → Gradle.
3. Em Gradle JDK, selecione uma instalação **21**.
4. Se ela não existir, use Download JDK, escolha versão 21 e conclua o download. Também é possível instalar [Eclipse Temurin 21](https://adoptium.net/temurin/releases/?version=21) e selecionar a pasta da instalação.
5. Em Apple Silicon, use JDK para macOS aarch64/ARM64; em Mac Intel, use x64. No Windows/Linux, escolha a arquitetura da máquina.
6. Se a opção de distribuição Gradle estiver disponível, use o Wrapper do projeto.
7. Clique em Apply/OK e em File → Sync Project with Gradle Files.

O JDK usado para executar o Android Studio pode ser diferente do JDK usado pelo Gradle. Não selecione automaticamente Embedded JDK, jbr ou JAVA_HOME sem confirmar que a versão apontada é 21. Configurar JAVA_HOME no terminal, sozinho, não corrige necessariamente o Gradle JDK da IDE.

### Instalar o SDK

Em Tools → SDK Manager:

- SDK Platforms: instale **Android 16 / API 36**.
- SDK Tools: instale **Android SDK Build-Tools 35.0.0** e **Android SDK Platform-Tools**. Use Show Package Details para escolher a versão dos Build Tools.
- Para emulação, instale também Android Emulator e uma imagem de sistema compatível com o processador do computador.
- Conclua a instalação e aceite as licenças apresentadas.

Confirme o Android SDK Location nessa tela. O Android Studio pode gerar local.properties com o caminho local do SDK. Esse arquivo não é versionado porque o caminho muda entre computadores.

### Selecionar o dispositivo e executar

Para um emulador, use Tools → Device Manager, crie um telefone com API 24 ou superior e aguarde a inicialização completa. A validação desta entrega usou API 36. Em Apple Silicon, selecione imagem ARM64; em computador Intel/AMD com suporte à virtualização, selecione a imagem adequada ao processador.

Para um aparelho físico, ative Opções do desenvolvedor → Depuração USB, conecte um cabo de dados e autorize a chave RSA na tela do aparelho. No Windows, pode ser necessário o driver USB do fabricante.

Após a sincronização, selecione a configuração **app**, escolha o dispositivo e pressione Run. A atividade inicial é com.example.pcbuilder.AtividadePrincipal.

Se aparecer somente Add Configuration, aguarde o fim da sincronização. Persistindo, crie em Run → Edit Configurations → + → Android App uma configuração para o módulo app, com Launch = Default Activity.

## Compilar pelo terminal

Execute os comandos na raiz do projeto. Ajuste os caminhos de JDK e SDK para as instalações do seu computador. As variáveis definidas abaixo valem para a sessão atual do terminal.

### Windows — PowerShell

Exemplo com JDK 21.0.10 instalado na pasta indicada:

```powershell
$env:JAVA_HOME = 'C:/Program Files/Java/jdk-21.0.10'
$env:ANDROID_HOME = "$env:LOCALAPPDATA/Android/Sdk"
$env:Path = "$env:JAVA_HOME/bin;$env:ANDROID_HOME/platform-tools;$env:Path"
java -version
.\gradlew.bat --version
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:prepararEntrega
```

Se o JDK foi baixado pela IDE, selecione o caminho mostrado em Gradle JDK em vez de copiar o caminho de exemplo. Confirme Java 21 e Gradle 8.13 nas saídas antes de compilar.

### macOS — Terminal

Depois de instalar o JDK 21:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export ANDROID_HOME="$HOME/Library/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
chmod +x gradlew
java -version
./gradlew --version
./gradlew :app:testDebugUnitTest
./gradlew :app:prepararEntrega
```

Se java_home informar que não há uma JVM compatível, instale o JDK 21 antes de repetir. O comando /usr/libexec/java_home é específico do macOS.

### Linux — Terminal

Exemplo de caminho de JDK 21 no Ubuntu/Debian x64; outras distribuições e arquiteturas podem usar outra pasta:

```bash
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-amd64"
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
chmod +x gradlew
java -version
./gradlew --version
./gradlew :app:testDebugUnitTest
./gradlew :app:prepararEntrega
```

Se o SDK estiver em outro local, use o Android SDK Location exibido pelo SDK Manager. JAVA_HOME deve apontar à raiz do JDK, que contém bin/java; não à pasta bin e não a um JRE.

### APK e instalação

A tarefa prepararEntrega compila o aplicativo e gera:

```text
build/entrega/PCBuilder-1.0-Projeto-Final.apk
```

O APK original do Android Gradle Plugin também fica em app/build/outputs/apk/debug/app-debug.apk. O arquivo de entrega é uma cópia desse APK de depuração assinado, destinada à avaliação acadêmica. Não é necessário configurar uma chave de assinatura privada para compilá-lo.

Com Platform-Tools no PATH e um dispositivo conectado:

```text
adb devices
adb install -r "build/entrega/PCBuilder-1.0-Projeto-Final.apk"
adb shell am start -n com.example.pcbuilder/.AtividadePrincipal
```

Se houver vários dispositivos, use o identificador mostrado por adb devices em cada comando. Exemplo: adb -s emulator-5554 install -r "build/entrega/PCBuilder-1.0-Projeto-Final.apk".

Também é possível transferir o APK para o celular e abri-lo pelo gerenciador de arquivos, autorizando a instalação por essa origem quando o Android solicitar. A distribuição para avaliação pode ser encontrada na página [Releases](https://github.com/victorgonzalez-prog/PCBuilder/releases).

## Testes e funcionamento esperado

Primeiro execute os testes locais. Depois, com emulador ou aparelho disponível, execute os testes de interface.

Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:prepararEntrega
.\gradlew.bat :app:connectedDebugAndroidTest
```

macOS/Linux:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:prepararEntrega
./gradlew :app:connectedDebugAndroidTest
```

Para consultar as dependências resolvidas, substitua a tarefa pelo comando :app:dependencies --configuration debugRuntimeClasspath. Para diagnósticos de compilação, adicione --stacktrace --console=plain.

Os testes Android cobrem orçamento, compatibilidade, serialização, carregamento, erro, lista vazia, nova consulta e as quatro telas. Utilizam fontes controladas e ofertas históricas reais em app/src/androidTest/assets/cotacoes-reais.json; não pesquisam na SerpApi e não substituem os preços do aplicativo de produção.

Verificação manual:

1. Catálogo: exibe oito categorias com um total de 37 componentes; Consultar preços apresenta carregamento e o resultado da API.
2. Minha Montagem: permite adicionar uma peça por categoria, remover e limpar; mostra avisos de compatibilidade e não apresenta um total completo se faltar cotação.
3. Orçamento: valida o valor digitado; gera apenas uma montagem completa compatível, dentro do orçamento e com preços aceitos; permite aplicar a montagem e copiar seu JSON.
4. Preços: exibe as ofertas aceitas, a data da coleta e o link de cada oferta. Sem cotação, mostra Sem cotação disponível.

A quantidade de peças com preço varia. A API usa cache por sete dias; cotações expiradas deixam de participar do cálculo até nova coleta administrativa. Sem preços suficientes, o gerador informa que não há montagem completa disponível. Isso é um estado previsto, sem valores inventados.

## Problemas e soluções

### Incompatible Gradle JVM version / Java 25 / Unsupported class file major version 69

O [issue #1 da prova parcial](https://github.com/victorgonzalez-prog/PCBuilder/issues/1) relata a falha ao abrir. A imagem associada mostra Gradle 8.13 executado com Java 25. **Essa combinação é incompatível.** Java 24 também não é a configuração desta entrega.

Selecione JDK 21 em Gradle JDK na IDE e configure JAVA_HOME para JDK 21 no terminal. Java 25 só tem suporte para executar Gradle a partir de 9.1; este projeto usa 8.13. A correção desta entrega é escolher o JDK adequado, sem migrar os plugins.

Após corrigir o JDK, execute .\gradlew.bat --stop no Windows ou ./gradlew --stop no macOS/Linux, confirme o JDK com --version e sincronize novamente. Se continuar apontando ao Java errado, confira a propriedade org.gradle.java.home no arquivo de usuário ~/.gradle/gradle.properties e o Gradle JDK da IDE. Remova ou corrija apenas a configuração local que aponta à instalação errada.

Referências: [compatibilidade Java/Gradle](https://docs.gradle.org/current/userguide/compatibility.html#java_runtime) e [seleção do JDK no Android Studio](https://developer.android.com/build/jdks#jdk-config-in-studio).

### JAVA_HOME is not set / JAVA_HOME is set to an invalid directory

Instale o JDK 21 e ajuste JAVA_HOME conforme o sistema operacional. No Windows, use aspas se houver espaços no caminho. Depois confirme java -version e o resultado de gradlew --version. Um caminho do computador do autor não deve ser copiado sem verificar se ele existe na máquina de avaliação.

### Permission denied: ./gradlew / bad interpreter com caractere de retorno

O Wrapper está versionado com permissão de execução e quebra de linha LF. Se a extração do ZIP perder a permissão, execute chmod +x gradlew. Se uma ferramenta tiver convertido o arquivo para CRLF, restaure-o do Git ou selecione LF no editor. Em macOS/Linux, use ./gradlew; em PowerShell, use .\gradlew.bat.

### Could not find or load main class org.gradle.wrapper.GradleWrapperMain

Confirme que o download/clone incluiu gradle/wrapper/gradle-wrapper.jar e gradle-wrapper.properties, além dos dois scripts gradlew. Faça um novo clone completo se algum desses arquivos estiver ausente; não é necessário instalar um Gradle global.

### SDK location not found / Failed to find Platform SDK with path platforms;android-36

Instale a plataforma API 36 e selecione o SDK no Android Studio. No terminal, configure ANDROID_HOME para o mesmo SDK Manager Location. Uma alternativa é um local.properties local com sdk.dir apontando a esse SDK. Exemplo Windows: sdk.dir=C:/Users/SEU_USUARIO/AppData/Local/Android/Sdk. No macOS, o caminho usual é /Users/SEU_USUARIO/Library/Android/sdk. Não versione esse arquivo nem reduza compileSdk para contornar a falta do pacote.

### Build Tools revision 35.0.0 ausente / licenças não aceitas

Abra SDK Manager → SDK Tools → Show Package Details, instale 35.0.0 e aceite as licenças. Confirme que a IDE e o terminal apontam ao mesmo SDK.

### AGP incompatível com Android Studio / módulo app não aparece

Use uma IDE que suporte AGP 8.12 e abra a pasta raiz correta. Se o projeto tiver sido aberto como uma pasta comum, feche-o e importe novamente a raiz com settings.gradle.kts. Após corrigir o JDK e o SDK, execute Sync Project with Gradle Files. Não procure MainActivity: a atividade desta versão chama-se AtividadePrincipal.

### Could not resolve / Unknown host / download do Gradle falhou / PKIX

A primeira sincronização precisa de internet. Desative o modo Offline do Gradle, confirme o acesso a services.gradle.org, dl.google.com, repo.maven.apache.org e plugins.gradle.org e tente novamente. Em rede com proxy, configure o proxy da IDE/Gradle; para erros de certificado, ajuste o certificado confiável da rede com o responsável pelo ambiente. Não desative a validação HTTPS.

Se o download foi interrompido, repita a tarefa com --refresh-dependencies. Caso seja preciso limpar arquivos do projeto, feche a IDE e remova somente .gradle e diretórios build gerados, depois sincronize; isso força recompilação e novos downloads. Não apague fontes, a pasta gradle/wrapper ou credenciais locais.

### Gradle sync failed sem detalhes / daemon encerrado / memória insuficiente

Abra a janela Build e leia o primeiro erro específico, ou execute a tarefa pelo terminal com --stacktrace --console=plain. Corrija primeiro JDK, SDK e downloads. Se o erro for OutOfMemoryError, feche programas/emuladores desnecessários; em máquina com memória disponível, ajuste org.gradle.jvmargs no gradle.properties de usuário. Usar aparelho físico reduz a necessidade de memória do emulador. A validação desta entrega usa limite de heap de 2 GB para o Gradle.

### adb não é reconhecido / no devices / unauthorized / more than one device

Instale Platform-Tools e inclua sua pasta no PATH ou use Run pela IDE. Inicie o emulador até a tela inicial, ou autorize a depuração USB no aparelho. Em Windows, confira cabo e driver. Para unauthorized, aceite a chave RSA no celular e reconecte. Havendo vários dispositivos, acrescente -s com o identificador correto. Estes comandos reiniciam apenas o serviço local de conexão do adb:

```text
adb kill-server
adb start-server
adb devices
```

### INSTALL_FAILED_OLDER_SDK / UPDATE_INCOMPATIBLE / VERSION_DOWNGRADE

OLDER_SDK exige aparelho Android API 24 ou superior. UPDATE_INCOMPATIBLE normalmente indica outra instalação de com.example.pcbuilder com assinatura diferente. Se for necessário substituí-la, desinstale o PCBuilder antigo e instale novamente; a desinstalação remove os dados locais desse aplicativo. VERSION_DOWNGRADE indica uma versão instalada mais nova: use a versão correta ou faça a mesma reinstalação consciente. Não altere o código para contornar esses erros de instalação.

### API demora / falha na consulta / não aparecem preços

Abra https://pcbuilder-api-dc2r.onrender.com/api/health e aguarde a inicialização do serviço gratuito. Quando responder online, volte ao aplicativo e toque em Consultar preços. Confirme a internet do aparelho/emulador e tente outra rede para distinguir uma restrição de rede de uma falha no serviço.

Se a API responder, consulte /api/precos. Estados sem_ofertas, em_revisao ou desatualizado não são preços válidos. Uma montagem automática pode ficar indisponível se uma categoria inteira não tiver cotação aceita. O responsável pelo backend deve conferir Redis, credenciais, validade do cache e a cota da SerpApi no painel administrativo. A leitura dos preços não dispara pesquisas automaticamente.

### Aplicativo ainda tenta localhost / Cleartext HTTP traffic not permitted

O padrão versionado é a URL HTTPS pública. Verifique se uma configuração de usuário ~/.gradle/gradle.properties ou uma opção -Ppcbuilder.apiUrl antiga está substituindo esse padrão, remova a substituição local e recompile/reinstale. Para testar backend local, use somente o procedimento da seção abaixo. Não use HTTP para a API pública.

### Não consigo gerar uma montagem / total indisponível

Informe orçamento positivo, por exemplo 5000 ou 5.000,00. Confirme as cotações em Preços. A montagem exige todas as categorias com peças compatíveis e preços disponíveis. Aumentar o orçamento pode resolver a falta de uma combinação dentro do limite, mas não cria cotação para peça sem preço. Minha Montagem não soma um total completo enquanto faltar cotação em alguma peça selecionada.

## Backend local opcional

Esta seção é para desenvolvimento ou administração da API. A avaliação do Android pode usar a API pública já configurada, sem esta instalação.

Na pasta backend, confira node --version e npm --version e instale as dependências bloqueadas pelo package-lock.json:

```text
npm ci
```

Copie backend/.env.example para backend/.env. No PowerShell, execute Copy-Item .env.example .env dentro de backend; no macOS/Linux, use cp .env.example .env. Configure os valores somente nesse arquivo local e execute:

```text
npm start
```

Rotas locais: http://localhost:3001/api/health, /api/catalogo, /api/precos e /api/status. A porta pode ser alterada pela variável PORT. Para EADDRINUSE, encerre apenas o servidor que ocupa a porta ou use outra porta e configure o cliente correspondente.

backend/cache-precos.json não é versionado. Um servidor novo sem cache local e sem Redis preenchido retorna os 37 componentes sem cotação; iniciar o servidor não faz pesquisas. A persistência remota usa UPSTASH_REDIS_REST_URL e UPSTASH_REDIS_REST_TOKEN. No Render, esse armazenamento é obrigatório; uma falha de autenticação no Redis impede a inicialização para evitar perda silenciosa dos dados.

Para apontar o APK ao backend local, com o dispositivo conectado:

Windows:

```powershell
adb reverse tcp:3001 tcp:3001
.\gradlew.bat :app:prepararEntrega "-Ppcbuilder.apiUrl=http://127.0.0.1:3001"
```

macOS/Linux:

```bash
adb reverse tcp:3001 tcp:3001
./gradlew :app:prepararEntrega "-Ppcbuilder.apiUrl=http://127.0.0.1:3001"
```

Reinstale o APK gerado. O backend deve continuar executando e adb reverse precisa ser refeito se a conexão mudar. Para retornar à API pública, compile sem a opção -P e reinstale. Não depende do projeto pessoal BuscarPreços.

### Pesquisas administrativas e credenciais

GET /api/health, /api/catalogo, /api/precos e /api/status são públicos. POST /api/atualizar e /api/atualizar/:id exigem Authorization: Bearer com ADMIN_API_TOKEN aleatório, de pelo menos 32 caracteres, diferente das credenciais SerpApi/Redis. Requisições sem senha são rejeitadas; sem configuração administrativa válida, as pesquisas ficam desabilitadas.

Para uma atualização administrativa, configure ADMIN_API_TOKEN e PCBUILDER_API_URL no backend/.env local, além da SerpApi no servidor, e execute dentro de backend:

```text
npm run atualizar
```

Sem a opção --forcar, essa operação respeita o prazo do cache. Uma nova pesquisa pode consumir a cota da SerpApi. HTTP 401 indica autenticação ausente/incorreta; HTTP 503 pode indicar configuração administrativa ou SerpApi ausente. Não coloque a senha administrativa no Android. Se a cota acabar, confira a conta do provedor; não substitua preços reais por estimativas.

Os arquivos .env, secrets.properties, credenciais, caches, node_modules e configurações locais são ignorados pelo Git. Se uma configuração privada for necessária ao professor para executar o backend opcional, entregue-a separadamente, conforme as regras da disciplina. Nunca a envie ao repositório ou a logs.

### Buscar preços atualizados na data da avaliação

**Se o professor quiser preços pesquisados na data em que estiver avaliando o aplicativo, deverá executar a mesma coleta administrativa descrita nesta seção.** O botão Consultar preços do Android lê as cotações armazenadas; a data de uma consulta ao aplicativo não significa que uma nova pesquisa foi realizada nas lojas.

1. Tenha Node.js e npm instalados conforme os pré-requisitos. Na pasta backend, execute npm ci para instalar as dependências.
2. Configure backend/.env com a URL abaixo e com ADMIN_API_TOKEN recebido separadamente, igual ao valor do serviço no Render. Mantenha a senha somente no arquivo local; ela não deve aparecer no README, no GitHub ou no APK.

```dotenv
PCBUILDER_API_URL=https://pcbuilder-api-dc2r.onrender.com
ADMIN_API_TOKEN=
```

Preencha o campo vazio com a senha administrativa real antes de executar. A chave SerpApi e as credenciais Redis já ficam no servidor hospedado; o comando de atualização da API pública utiliza o token administrativo local.

3. No terminal, a partir da raiz do projeto, execute:

```text
cd backend
npm run atualizar -- --forcar --acompanhar
```

O comando utiliza a API hospedada. --forcar solicita uma nova pesquisa dos 37 componentes mesmo que o cache de sete dias ainda esteja válido, e usa no_cache=true para solicitar resultados novos ao provedor. --acompanhar consulta /api/status e apresenta o progresso, os erros, a quantidade de componentes com oferta aceita e as datas das cotações disponíveis. [Referência do parâmetro no_cache da SerpApi](https://serpapi.com/google-shopping-api).

4. Aguarde a conclusão e confira o resultado. Uma coleta completa deve indicar 37 de 37 componentes verificados e nenhum erro de pesquisa. Depois, abra Preços no aplicativo e toque em Consultar preços para carregar o resultado e verificar a data exibida em Coleta.

A coleta real consome a cota do provedor e mantém os limites de segurança existentes. Ela pode terminar sem ofertas válidas para algumas peças, que continuarão como Sem cotação disponível. Não há garantia de encontrar preços para todos os modelos. Se houver erros ou a coleta parar antes dos 37 componentes, confira /api/status e a cota antes de repetir; o comando retorna erro para uma coleta incompleta. Se o acompanhamento atingir 15 minutos, ele informa o limite de espera e a pesquisa pode continuar no servidor.

Sem --forcar, o comando pode retornar que o cache ainda está válido e manter datas anteriores. O procedimento de coleta forçada é o necessário para solicitar uma nova pesquisa na data atual; acessar a página da API ou o botão do Android apenas consulta os dados existentes.

### Arquivo de configuração local

O arquivo de credenciais usado por este projeto é **backend/.env**. Ele cumpre a função do arquivo local citado nas condições do trabalho; secrets.properties é outro exemplo de nome, mas não é necessário criar um segundo arquivo de segredos para este backend Node.js. O modelo público é [backend/.env.example](backend/.env.example), que contém somente nomes de variáveis e valores vazios para as credenciais.

| Campo do arquivo local | Utilização |
| --- | --- |
| SERPAPI_KEY | Chave do provedor, usada pelo servidor ao pesquisar novos preços. |
| UPSTASH_REDIS_REST_URL | Endereço do armazenamento persistente Redis. |
| UPSTASH_REDIS_REST_TOKEN | Credencial de acesso ao Redis; configure junto com sua URL. |
| ADMIN_API_TOKEN | Senha própria de pelo menos 32 caracteres para autorizar pesquisas administrativas, diferente das credenciais dos provedores. |
| PCBUILDER_API_URL | Endereço da API usado pelo comando administrativo npm run atualizar; não é uma senha. |

Para executar somente o Android com a API pública padrão, não há arquivo de credenciais a configurar no aplicativo. Para executar o backend com serviços privados, coloque o arquivo recebido separadamente em backend/.env e siga a seção Backend local opcional. Para administrar a API hospedada, ADMIN_API_TOKEN deve corresponder ao configurado no Render. Para administrar um backend local próprio, configure uma senha administrativa própria nesse servidor.

A propriedade Gradle pcbuilder.apiUrl configura o endereço público usado pelo APK. Ela é independente de PCBUILDER_API_URL, que pertence ao comando administrativo do backend. As chaves da SerpApi, do Redis e a senha administrativa permanecem no servidor e fora dos fontes Android. Não inclua o arquivo privado em commits, Releases ou arquivos ZIP públicos.

### Testes do backend

Dentro de backend:

```text
npm test
```

Os testes cobrem autenticação, respostas públicas, proteção contra exposição do corpo inválido, rejeição de anúncios de kits vendidos como peça avulsa e coleta forçada com acompanhamento. Os testes de pesquisa usam respostas simuladas sem rede externa e sem uma chave SerpApi válida; não consomem sua cota.

## Hospedagem no Render

O serviço se chama pcbuilder-api. A configuração versionada está em render.yaml: pasta backend, npm ci, npm start, plano Free e health check /api/health.

Para reproduzir a hospedagem em outra conta, use New → Blueprint, conecte o repositório e informe SERPAPI_KEY, UPSTASH_REDIS_REST_URL e UPSTASH_REDIS_REST_TOKEN somente nos campos secretos do painel. O Blueprint gera ADMIN_API_TOKEN automaticamente. Aguarde Live antes de testar a URL gerada. Se usar outra URL, informe-a na propriedade Gradle pcbuilder.apiUrl e recompile, sem colocar credenciais no aplicativo.

As 37 consultas locais foram transferidas para o Redis sem prazo de expiração do registro. Após reinício e nova publicação do serviço, a API recarregou as cotações. O prazo de validade das ofertas é controlado separadamente pela aplicação. O Render gratuito pode suspender o servidor por inatividade; a primeira consulta pode demorar enquanto ele inicia. Depois de transferir um cache para um serviço já ligado, use Manual Deploy → Restart service para recarregá-lo.

## Arquitetura e itens do trabalho

- Uma única atividade, AtividadePrincipal, com quatro telas em Jetpack Compose e Navigation 3.
- Listas e categorias com LazyColumn e LazyVerticalGrid; não há layouts XML de interface.
- ModeloPcBuilder usa ViewModel, StateFlow e Coroutines para consultas e geração de montagens, com estados de carregamento, erro, vazio e sucesso.
- CatalogoComponentes contém 37 peças; VerificadorCompatibilidade e GeradorMontagem avaliam as seleções.
- ExportadorMontagem serializa e desserializa JSON usando kotlinx.serialization; dados opcionais e ausência de cotação são tratados explicitamente.
- O backend Node.js consulta preços reais, filtra correspondências e kits indevidos e mantém cache persistente. Kits de memória da capacidade/geração corretas continuam pesquisáveis.

Arquivos XML de manifesto, tema, ícones e regras de backup são recursos Android, não layouts de tela. Os nomes próprios do código e textos da interface estão em português; APIs, bibliotecas e protocolos preservam seus nomes originais.

### Fluxo de dados e decisões técnicas

[AtividadePrincipal](app/src/main/java/com/example/pcbuilder/AtividadePrincipal.kt) cria a interface Compose e obtém uma instância de ModeloPcBuilder associada à atividade. [AplicativoPcBuilder](app/src/main/java/com/example/pcbuilder/AplicativoPcBuilder.kt) apresenta as quatro rotas com NavDisplay e Navigation 3. As telas compartilham esse mesmo modelo de estado ao navegar.

Ao tocar em Consultar preços, a tela chama o ViewModel. ModeloPcBuilder usa viewModelScope para iniciar a coroutine, marca o carregamento e consulta a interface FonteCotacoes. A implementação HTTP ServicoFinal executa a requisição em Dispatchers.IO. O resultado atualiza os StateFlow de cotações e status; os composables observam esses valores e apresentam carregamento, sucesso, ausência de preços ou erro. O uso de ViewModel mantém a seleção fora dos composables, e FonteCotacoes permite substituir a rede por respostas controladas nos testes.

Ao gerar uma montagem, o ViewModel seleciona apenas componentes com cotação aceita e passa essas peças ao [GeradorMontagem](app/src/main/java/com/example/pcbuilder/GeradorMontagem.kt). O cálculo roda em Dispatchers.Default para liberar a interface durante o processamento. Orçamento, finalidade e restrições de compatibilidade orientam a escolha. A tela Minha Montagem apresenta os avisos do [VerificadorCompatibilidade](app/src/main/java/com/example/pcbuilder/VerificadorCompatibilidade.kt). O resultado pode ser aplicado à seleção compartilhada. O estado de seleção pertence ao ViewModel; a persistência das cotações pertence ao backend.

CotacaoFinal.preco é opcional: só fornece um preço quando o status da API é com_ofertas e existe uma oferta com valor finito e positivo. Dados opcionais como URL e data de coleta são tratados antes da exibição. Essa decisão impede que a ausência de cotação se transforme em preço zero ou total aparentemente completo.

O JSON recebido da API é interpretado com org.json. A exportação da montagem usa [ExportadorMontagem](app/src/main/java/com/example/pcbuilder/ExportadorMontagem.kt), os modelos anotados com @Serializable e kotlinx.serialization. O método exportar usa encodeToString, e importar usa decodeFromString. A interface permite copiar o JSON; a desserialização é exercitada nos testes.

No servidor, [servidor.js](backend/servidor.js) mantém o catálogo e aplica os filtros às ofertas antes de devolvê-las ao aplicativo. [persistencia.js](backend/persistencia.js) carrega e salva o cache em Redis ou em arquivo local, conforme a configuração. [seguranca.js](backend/seguranca.js) protege as rotas que iniciam pesquisas. O aplicativo lê o cache por GET; a coleta na SerpApi é uma operação administrativa separada. Assim, consultar preços pelo Android não gasta pesquisas do provedor nem exige entregar uma chave dentro do APK.

A compatibilidade é preliminar: confirme BIOS, dimensões, conexões e refrigeração antes de comprar. As ofertas refletem o momento da coleta e não garantem estoque ou preço na compra.

## Ambiente de validação e limites

A validação local usa Windows, JDK 21.0.10, Gradle 8.13, SDK 36/Build Tools 35.0.0, Android Studio build AI-262.10968.63.2623.16840739 e emulador Android 16/API 36. O backend foi validado com Node.js 22.22.2 e npm 10.9.7.

Uma cópia contendo somente os arquivos preparados para o Git foi compilada sem .env, sem local.properties e com um GRADLE_USER_HOME novo. O Wrapper e as dependências foram baixados novamente. Nessa cópia passaram seis testes locais Android, quatro testes de interface no emulador e sete testes do backend após npm ci; a tarefa prepararEntrega também gerou o APK com o nome da entrega.

Os procedimentos de macOS e Linux estão documentados; não houve execução real nesses dois sistemas nesta validação. A compilação depende dos downloads iniciais e da instalação correta do SDK/JDK. O funcionamento de preços depende da disponibilidade da API e das cotações; os estados sem cotação são previstos. Antes da apresentação, confirme a validade do cache e teste o APK também em um aparelho físico e outra rede.
