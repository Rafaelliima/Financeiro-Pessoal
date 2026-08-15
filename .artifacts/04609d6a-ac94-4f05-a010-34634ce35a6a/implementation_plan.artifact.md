# Plano de Implementação: Sistema de Lembretes, Lógica de Faturas e Gestão de Cartões

Este plano detalha a adição de um sistema de lembretes, a correção da lógica de parcelamento e faturas, e a separação da gestão de cartões da visualização de faturas.

## User Review Required

> [!IMPORTANT]
> A lógica de faturas passará a ocultar compras cujo primeiro pagamento é futuro. Pagar a fatura do mês atual afetará apenas as compras que possuem parcelas vigentes no período.

> [!NOTE]
> Lembretes pagos serão convertidos automaticamente em "Gastos do Dia a Dia", mantendo o histórico de pagamento.

## Proposed Changes

---

### 1. Sistema de Lembretes

#### [MODIFY] [Models.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/data/Models.kt)
- Adicionar data class `ReminderItem` com campos: `id`, `name`, `value`, `date`, `isPaid`.

#### [MODIFY] [StorageData.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/data/StorageData.kt)
- Adicionar `reminders: List<ReminderItem>` à classe `StorageData`.
- Incrementar `CURRENT_VERSION` para 13.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/DashboardScreen.kt)
- Implementar a seção **Lembretes** com visual moderno.
- Adicionar lógica para exibir apenas lembretes pendentes (ou todos, com distinção visual).
- Adicionar callback `onPayReminder(ReminderItem)` que marcará como pago e converterá em gasto diário.

---

### 2. Correção de Lógica de Faturas e Parcelas

#### [MODIFY] [Models.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/data/Models.kt)
- Refinar `calculateInstallments` para garantir que compras com `startMonth/Year` no futuro retornem status "Futura" e não apareçam nos cálculos do mês atual.

#### [MODIFY] [PurchasesScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/PurchasesScreen.kt)
- Filtrar compras na aba "Por Cartão" para exibir apenas as que pertencem ao mês de referência (Remover compras futuras da lista da fatura atual).

#### [MODIFY] [MainActivity.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/MainActivity.kt)
- Atualizar `registerInvoicePayment` para incrementar `paidInstallmentsCount` **apenas** se a compra tiver uma parcela ativa no mês que está sendo pago.

---

### 3. Gerenciamento de Cartões

#### [MODIFY] [PurchasesScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/PurchasesScreen.kt)
- Adicionar o botão **⚙️ Configurações de cartões** abaixo da lista de cartões na aba "Por Cartão".
- Implementar um novo `ModalBottomSheet` ou Dialog dedicado exclusivamente para Adicionar, Editar e Excluir cartões.

---

## Verification Plan

### Automated Tests
- Validar via unit test que `calculateInstallments` retorna status correto para datas futuras.
- Simular pagamento de fatura em `MainActivity` e conferir se compras futuras permanecem inalteradas.

### Manual Verification
1. Criar lembrete e verificar se o total do Dashboard não muda.
2. Pagar o lembrete e verificar se ele aparece em "Dia a Dia" e altera o saldo.
3. Cadastrar compra para o mês seguinte e verificar se ela está oculta na fatura deste mês.
4. Pagar fatura atual e verificar se a compra futura continua na parcela 1 para o mês que vem.
5. Acessar "Configurações de cartões" e editar o nome de um cartão.
