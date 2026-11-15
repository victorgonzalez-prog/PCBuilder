# PCBuilder

Aplicativo Android acadêmico para selecionar peças de computador, verificar compatibilidade, gerar montagens por orçamento e comparar cotações reais.

## Funcionalidades e arquitetura

- Quatro telas em Jetpack Compose: Catálogo, Minha Montagem, Orçamento e Preços.
- Navigation 3 em uma única atividade: AtividadePrincipal.
- ModeloPcBuilder gerencia o estado com ViewModel, StateFlow e Coroutines.
- CatalogoComponentes contém 37 peças. GeradorMontagem e VerificadorCompatibilidade avaliam as configurações.
- ExportadorMontagem serializa e desserializa configurações com kotlinx.serialization.
- API Node.js independente em backend/servidor.js, com persistência e proteção das pesquisas.

Os nomes próprios do código e os textos da interface estão em português. As bibliotecas, os protocolos e as configurações externas mantêm seus nomes originais.

Anúncios de kits com peças extras não são aceitos como preço de um componente avulso. Kits de memória da capacidade e geração corretas permanecem pesquisáveis.

Sem cotação aceita, o aplicativo exibe “Sem cotação disponível”. O total fica indisponível quando falta preço, e a montagem automática só usa peças com preços finitos e positivos. Compatibilidade é preliminar: confirme BIOS, dimensões, conexões e refrigeração antes da compra.

## Pré-requisitos

- Android Studio, JDK 21 e Android SDK 36.
- Emulador ou aparelho com Android 7.0 (API 24) ou superior.
- Node.js 20 ou superior e npm para executar o backend local.
- Chave SerpApi apenas para pesquisar novos preços. A consulta ao cache não consome pesquisas.

## Executar o backend local

Na pasta backend:

```powershell
npm ci
Copy-Item .env.example .env
```

Preencha o arquivo .env local quando forem necessárias credenciais e execute:

```powershell
npm start
```

A API responde em http://localhost:3001/api/health, /api/catalogo, /api/precos e /api/status.

O cache backend/cache-precos.json não é versionado. Sem cache ou armazenamento remoto preenchido, a API retorna componentes sem cotação. Iniciar o servidor não pesquisa preços automaticamente.

Os segredos ficam exclusivamente no servidor ou na configuração administrativa local. Envie ao professor a configuração necessária separadamente, conforme as regras do trabalho. Nunca coloque credenciais no APK, no repositório ou em logs.

## Compilar e executar o Android

Configure o Gradle para usar JDK 21. No PowerShell, ajuste o caminho se sua instalação for diferente:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21.0.10'
```

O projeto usa por padrão a API pública https://pcbuilder-api-dc2r.onrender.com, configurada em gradle.properties. Para usar o aplicativo com essa API, o professor não precisa executar Node.js nem configurar chaves no Android.

Na raiz do projeto, com o dispositivo conectado:

```powershell
.\gradlew.bat :app:assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.example.pcbuilder/.AtividadePrincipal
```

Também é possível abrir o projeto no Android Studio e executar o módulo app. O APK é gerado em app/build/outputs/apk/debug/app-debug.apk.

Para desenvolver usando o backend local em execução:

```powershell
adb reverse tcp:3001 tcp:3001
.\gradlew.bat :app:assembleDebug "-Ppcbuilder.apiUrl=http://127.0.0.1:3001"
```

A propriedade pcbuilder.apiUrl permite escolher outra API sem alterar código Kotlin. O endereço público não é um segredo. Credenciais permanecem exclusivamente no servidor.

## Pesquisas administrativas

As rotas POST /api/atualizar e /api/atualizar/:id exigem Authorization: Bearer com ADMIN_API_TOKEN. Essa senha deve ser aleatória, com pelo menos 32 caracteres e diferente das credenciais da SerpApi e do Redis. Sem configuração válida, as pesquisas ficam desabilitadas. O aplicativo apenas consulta cotações armazenadas.

Para solicitar uma pesquisa, preencha ADMIN_API_TOKEN e PCBUILDER_API_URL no backend/.env local e execute na pasta backend:

```powershell
npm run atualizar
```

Esse comando pode consumir a cota da SerpApi. Ele respeita o prazo do cache e não força pesquisas de registros ainda válidos. Acompanhe /api/status e consulte novamente os preços no aplicativo.

## Testes

Na pasta backend:

```powershell
npm test
```

Na raiz, execute os testes locais antes dos testes com dispositivo:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest "-Ppcbuilder.apiUrl=http://127.0.0.1:3001"
```

Os testes cobrem regras de orçamento, compatibilidade, serialização, ausência de preços, estados da consulta, navegação e autenticação das pesquisas. Eles não consultam a SerpApi. A amostra em app/src/androidTest/assets/cotacoes-reais.json contém ofertas históricas reais exclusivas dos testes e não alimenta o aplicativo de produção.

## Publicar no Render

1. Envie a versão final ao GitHub, preservando o histórico da prova parcial.
2. No Render, use New > Blueprint, conecte o repositório e selecione render.yaml na raiz.
3. Informe somente no painel SERPAPI_KEY, UPSTASH_REDIS_REST_URL e UPSTASH_REDIS_REST_TOKEN.
4. O Blueprint gera ADMIN_API_TOKEN automaticamente e prepara Node.js, pasta backend, npm ci, npm start e verificação em /api/health.
5. Transfira as cotações reais já coletadas para o Redis usando configuração local privada. Se a transferência ocorrer após iniciar o serviço, reinicie-o para recarregar o cache.
6. Verifique os 37 componentes em /api/catalogo, os estados em /api/precos e a rejeição de pesquisas sem senha.
7. Compile o Android com a URL HTTPS gerada e teste em outra rede.

A API está publicada em https://pcbuilder-api-dc2r.onrender.com. As rotas públicas respondem e as pesquisas administrativas sem autenticação são rejeitadas. As 37 consultas locais foram transferidas e confirmadas no Redis sem prazo de expiração. Após reiniciar o serviço, a API recarregou as cotações persistidas. A consulta HTTPS também foi validada no Android com o backend local desligado.

O Render gratuito pode suspender o servidor por inatividade; a primeira consulta pode demorar enquanto ele inicia. Sem cotações armazenadas, o aplicativo apresenta “Sem cotação disponível” e mantém o catálogo e a seleção de peças.

Entregue o link do repositório no Classroom. As cotações refletem o momento da coleta e não garantem estoque ou preço na compra.
