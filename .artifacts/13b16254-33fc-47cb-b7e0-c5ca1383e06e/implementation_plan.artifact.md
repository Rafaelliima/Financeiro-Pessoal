# Implementação de Identidade Visual Inteligente para Bancos

Este plano detalha a criação de um registro de marcas de bancos para aplicar cores automáticas aos cartões baseando-se no nome, melhorando a identificação visual sem a necessidade de carregar imagens da internet.

## Proposta de Mudanças

### [UI & Theme]

#### [NEW] [BankBrandRegistry.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/theme/BankBrandRegistry.kt)
Criar um utilitário que mapeia nomes de bancos para suas respectivas cores de marca.
*   Bancos suportados inicialmente: Nubank, Itaú, Inter, Bradesco, Mercado Pago, Santander, Caixa, Banco do Brasil.
*   Retornará uma cor principal e uma cor de contraste para o texto/ícone.

#### [MODIFY] [CardsScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/CardsScreen.kt)
*   Atualizar o componente `CardRowItem` para usar as cores do banco no ícone ou em um indicador visual.
*   Melhorar a visualização do card para que ele pareça mais um "cartão" físico (usando cores de fundo leves ou bordas coloridas).

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/DashboardScreen.kt)
*   Atualizar a seção de "Cartões" no Dashboard para exibir o pequeno indicador colorido ao lado do nome do cartão, mantendo a consistência visual.

## Verificação Plan

### Manual Verification
1.  Abrir a tela de Cartões.
2.  Adicionar um novo cartão com o nome "Nubank".
3.  Verificar se o ícone ou borda assume a cor roxa.
4.  Adicionar um cartão "Itaú" e verificar a cor laranja.
5.  Verificar se no Dashboard as cores também são refletidas.
