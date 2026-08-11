# Plano de Implementação - Correção de Downloads e Atualizações

Este plano visa corrigir o problema de falha no download do APK em dispositivos Android e otimizar o fluxo de atualização para que o app solicite a instalação automaticamente após o download.

## Análise do Problema

1.  **Falha no Download:** O `DownloadManager` pode falhar por diversos motivos no Android:
    *   Falta de permissão para tráfego HTTP (Cleartext Traffic) se a URL não for HTTPS.
    *   Configuração incorreta do destino do arquivo no `DownloadManager`.
    *   Configuração do `BroadcastReceiver` no Android 14+ (precisa ser `RECEIVER_EXPORTED` para receber sinal do sistema).
2.  **Fluxo de Instalação:** O usuário deseja que o app "tente instalar sozinho, mas perguntando". O fluxo atual já possui o diálogo de confirmação, mas vamos garantir que a transição entre download e instalação seja robusta.

## Mudanças Propostas

### Sistema e Manifesto

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/AndroidManifest.xml)
- Adicionar `android:usesCleartextTraffic="true"` para permitir downloads de links HTTP caso necessário.
- Garantir que `REQUEST_INSTALL_PACKAGES` e `INTERNET` estejam presentes (já estão).

#### [MODIFY] [file_paths.xml](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/res/xml/file_paths.xml)
- Adicionar `<external-files-path>` para garantir que o `FileProvider` consiga acessar a pasta de downloads do app onde o APK será salvo.

### Lógica de Negócio

#### [MODIFY] [UpdateManager.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/data/UpdateManager.kt)
- Alterar `setDestinationUri` para `setDestinationInExternalFilesDir`, que é mais seguro para permissões modernas.
- Mudar o registro do `BroadcastReceiver` para `RECEIVER_EXPORTED` para garantir que o sistema consiga avisar o app quando o download terminar.
- Adicionar logs extras para diagnosticar falhas no `DownloadManager`.

## Plano de Verificação

### Testes Manuais
1.  Subir uma nova versão no Firestore com um `versionCode` superior ao atual.
2.  Abrir o app e verificar se o diálogo "Nova Atualização!" aparece.
3.  Clicar em "Atualizar agora".
4.  Observar a barra de notificações para ver se o download inicia e completa.
5.  Verificar se, ao terminar, o sistema abre automaticamente a tela de instalação ("Deseja instalar uma atualização para este aplicativo?").

> [!IMPORTANT]
> A instalação de APKs fora da Google Play exige que o usuário conceda a permissão "Instalar aplicativos desconhecidos" para o seu app. O Android solicitará isso automaticamente na primeira vez que a instalação for tentada.
