# Releitura Moderna e Personalização do App

Este plano foca em transformar o visual "genérico" do aplicativo em uma interface moderna, sofisticada e personalizada, além de adicionar controle manual sobre o tema (Modo Escuro).

## User Review Required

> [!IMPORTANT]
> O design passará por uma mudança radical de paleta e formas. O modo escuro deixará de ser o cinza padrão do Android para usar tons de azul profundo e grafite, com acentos vibrantes.

## Proposed Changes

---

### Design System & Theme

#### [MODIFY] [Color.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/theme/Color.kt)
- Redefinir paletas:
    - **Escuro:** Fundo `#0D0F12` (azul-noite profundo), Superfícies `#161B22`, Acento `#38BDF8`.
    - **Claro:** Fundo `#F1F5F9` (cinza azulado suave), Superfícies `#FFFFFF`, Acento `#0284C7`.
- Adicionar cores de "Glassmorphism" (transparências calculadas).

#### [MODIFY] [Theme.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/theme/Theme.kt)
- Ajustar `FinanceiroPessoalTheme` para aceitar um estado de `ThemeMode` (Claro, Escuro, Sistema).

#### [MODIFY] [Type.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/theme/Type.kt)
- **Hierarquia Visual:** Refinar pesos (`FontWeight`) e espaçamentos (`lineHeight`, `letterSpacing`) para criar uma distinção clara entre informações primárias e secundárias.
- **Legibilidade:** Aumentar o `lineHeight` do texto de corpo para dar "respiro" ao layout.
- **Estilo Moderno:** Ajustar o `headlineLarge` para um visual mais condensado e impactante (estilo interface de bancos digitais).

---

### UI Components Overhaul

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/DashboardScreen.kt)
- **Disposição de Textos:** Reorganizar os cards para que as informações mais importantes (valores) tenham destaque imediato, enquanto rótulos auxiliares usem fontes menores e opacidade reduzida.
- Criar um card "Hero" para o saldo total com gradiente sutil.
- Aumentar o `RoundedCornerShape` para `28.dp`.
- Remover divisores pesados e usar elevação/bordas sutis.

#### [MODIFY] [PurchasesScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/PurchasesScreen.kt)
- **Layout de Listas:** Melhorar o alinhamento das linhas de compra. Valores à direita em negrito, nomes à esquerda com subtítulos claros.
- Estilizar as abas internas com animações de transição mais suaves.
- Melhorar o visual dos cards de cartões (menus expansíveis) com cores de marca mais integradas.

---

### Features & Settings

#### [MODIFY] [MainActivity.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/MainActivity.kt)
- Gerenciar o estado global do tema (`themeMode`) e persistir no `JsonStorageManager`.

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/jrafa/OneDrive/Documents/GitHub/Financeiro-Pessoal/app/src/main/java/com/example/ui/screens/SettingsScreen.kt)
- Adicionar seção "Aparência" com um Switch/Toggle para o Modo Escuro.

---

## Verification Plan

### Manual Verification
1. Abrir **Ajustes** e alternar o Modo Escuro manualmente.
2. Verificar se as cores de azul profundo são aplicadas corretamente no tema escuro.
3. Observar se os cards do **Dashboard** possuem cantos mais arredondados e visual limpo.
4. Validar se o gradiente no saldo principal está legível em ambos os temas.
