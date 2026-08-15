# Plano de Implementação - Correções do Aplicativo Financeiro

Este plano detalha as alterações necessárias para implementar as três melhorias solicitadas: seletor visual de cores para cartões, responsividade das telas ao teclado e padronização das datas para o formato brasileiro (DD/MM/AAAA).

## Mudanças Propostas

### 1. Melhoria na Seleção de Cores dos Cartões

O objetivo é remover a entrada manual de código hexadecimal e substituir por um seletor visual com 10 cores predefinidas.

#### [MODIFICAR] [PurchasesScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/PurchasesScreen.kt)
- Atualizar `CardFormDialog` para:
    - Remover o `OutlinedTextField` do código hexadecimal.
    - Adicionar uma `LazyRow` com o seletor de cores predefinidas, similar ao que já existe em `CardsScreen.kt`.
    - Garantir que a cor selecionada seja visualmente destacada (usando uma borda ou ícone de check).
    - Corrigir a inicialização da cor para evitar crashes com o `Color.parseColor`.

#### [MODIFICAR] [CardsScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/CardsScreen.kt)
- Verificar e reforçar o `CardFormDialog` existente para garantir que ele atenda a todos os requisitos (destaque visual, remoção de hex se houver, etc.).

---

### 2. Responsividade das Telas ao Teclado Virtual

O objetivo é garantir que os campos de entrada não sejam cobertos pelo teclado e que o usuário consiga fazer scroll nos formulários quando necessário.

#### [MODIFICAR] [PurchasesScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/PurchasesScreen.kt)
- Envolver o conteúdo de `PurchaseFormContent`, `DailyExpenseFormContent` e `SubscriptionFormContent` em um `Column` com `Modifier.verticalScroll(rememberScrollState())`.
- Garantir o uso correto de `imePadding()` para empurrar o conteúdo para cima.

#### [MODIFICAR] [DashboardScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/DashboardScreen.kt)
- Em `AddReminderDialog`, envolver o conteúdo do `AlertDialog` em um `Column` com `verticalScroll` e aplicar `imePadding()`.

#### [MODIFICAR] [CardsScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/CardsScreen.kt)
- Em `CardFormDialog`, adicionar `verticalScroll` e `imePadding()` ao container principal do formulário.

---

### 3. Padronização do Formato de Datas (BR: DD/MM/AAAA)

O objetivo é exibir e permitir a entrada de datas no formato brasileiro, mantendo o armazenamento interno estruturado (ISO/YYYY-MM-DD).

#### [MODIFICAR] [DashboardScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/DashboardScreen.kt)
- Em `AddReminderDialog`:
    - Mudar o `SimpleDateFormat` inicial para `dd/MM/yyyy`.
    - Atualizar o label do campo de data para "Data (DD/MM/AAAA)".
    - Adicionar lógica de conversão no `onClick` do botão Salvar para transformar de `dd/MM/yyyy` para `yyyy-MM-dd` antes de enviar para o `onConfirm`.
- Na exibição dos lembretes (`ReminderRowItem` ou similar):
    - Formatar a data para `dd/MM/yyyy` antes de exibir.

#### [MODIFICAR] [PurchasesScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/PurchasesScreen.kt)
- Em `DailyExpenseFormContent`:
    - Mudar a máscara de exibição e entrada da data para `dd/MM/yyyy`.
    - Garantir a conversão correta ao salvar.

## Plano de Verificação

### Testes Manuais
1. **Seletor de Cores**:
    - Abrir o cadastro de cartões em ambos os locais (Configurações e Cadastro de Compra).
    - Verificar se o campo hexadecimal sumiu.
    - Selecionar uma cor e verificar se a prévia do cartão é atualizada.
    - Salvar e verificar se a cor persiste.

2. **Teclado Virtual**:
    - Abrir o formulário de Compra Parcelada.
    - Ativar o parcelamento e clicar nos campos de parcelas/data.
    - Verificar se a tela permite scroll e o campo focado fica visível acima do teclado.
    - Repetir em Lembretes e Gastos Diários.

3. **Formato de Data**:
    - Cadastrar um lembrete com a data `14/08/2026`.
    - Salvar e verificar se na lista aparece como `14/08/2026`.
    - Editar o lembrete e verificar se a data continua carregando corretamente no formato BR.
    - Repetir para Gastos Diários.

### Verificação Técnica
- Garantir que `Color.parseColor` receba apenas strings válidas ou usar um fallback seguro para evitar crashes.
- Validar as conversões de data para evitar inversão de dia/mês.
