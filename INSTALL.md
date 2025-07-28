# Guia de Instalação - Free Fire Aimbot

Este guia fornece instruções passo a passo para instalar e configurar o Free Fire Aimbot em seu dispositivo Android.

## 📋 Pré-requisitos

### Requisitos do Sistema
- **Android**: Versão 5.0+ (API 21+)
- **RAM**: Mínimo 2GB, recomendado 4GB+
- **Armazenamento**: 500MB livres
- **Processador**: Snapdragon 660+ ou equivalente
- **Conexão**: Internet para download inicial do modelo

### Aplicativos Necessários
1. **Shizuku** - Para permissões privilegiadas
2. **Free Fire** - O jogo alvo
3. **Gerenciador de arquivos** - Para instalação manual (opcional)

## 🔧 Preparação do Dispositivo

### 1. Ativar Modo Desenvolvedor
1. Vá para **Configurações** > **Sobre o telefone**
2. Toque 7 vezes em **Número da versão**
3. Volte para **Configurações** > **Opções do desenvolvedor**
4. Ative **Depuração USB**
5. Ative **Instalar via USB** (se disponível)

### 2. Permitir Fontes Desconhecidas
1. Vá para **Configurações** > **Segurança**
2. Ative **Fontes desconhecidas** ou **Instalar apps desconhecidos**
3. Para Android 8+: Configure por aplicativo conforme necessário

### 3. Configurar ADB (Opcional)
Se você tem acesso a um computador:
```bash
# Baixar Android Platform Tools
# https://developer.android.com/studio/releases/platform-tools

# Conectar dispositivo via USB
adb devices

# Verificar conexão
adb shell echo "Conectado com sucesso"
```

## 📱 Instalação do Shizuku

### Método 1: Via ADB (Recomendado)
1. **Baixar Shizuku**:
   - Acesse: https://github.com/RikkaApps/Shizuku/releases
   - Baixe a versão mais recente (.apk)

2. **Instalar Shizuku**:
   ```bash
   adb install shizuku-v13.x.x.apk
   ```

3. **Iniciar Serviço Shizuku**:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```

### Método 2: Via Root (Alternativo)
Se seu dispositivo tem root:
1. Instale o Shizuku normalmente
2. Abra o aplicativo
3. Toque em "Iniciar via root"
4. Conceda permissões de superusuário

### Método 3: Via Wireless ADB (Android 11+)
1. Ative **Depuração wireless** nas opções do desenvolvedor
2. Conecte via WiFi usando ADB
3. Execute os comandos do Método 1

## 🚀 Instalação do Free Fire Aimbot

### 1. Download do APK
- Baixe o arquivo `freefireaimbot-release.apk`
- Ou compile do código fonte (veja seção de desenvolvimento)

### 2. Instalação Manual
1. Copie o APK para o dispositivo
2. Abra o gerenciador de arquivos
3. Navegue até o arquivo APK
4. Toque para instalar
5. Confirme a instalação

### 3. Instalação via ADB
```bash
adb install freefireaimbot-release.apk
```

## ⚙️ Configuração Inicial

### 1. Primeira Execução
1. Abra o **Free Fire Aimbot**
2. Leia e aceite os termos de uso
3. O aplicativo solicitará várias permissões

### 2. Configurar Permissões

#### Permissão de Overlay
1. Quando solicitado, toque em **Configurações**
2. Encontre **Free Fire Aimbot** na lista
3. Ative **Permitir sobreposição**
4. Volte para o aplicativo

#### Permissão de Captura de Tela
1. Toque em **Solicitar Captura de Tela**
2. Confirme na caixa de diálogo do sistema
3. A permissão será concedida automaticamente

#### Permissões Shizuku
1. Certifique-se de que o Shizuku está rodando
2. Toque em **Solicitar Permissões Shizuku**
3. Confirme no aplicativo Shizuku
4. Aguarde a confirmação

### 3. Configurações Básicas
1. Vá para **Configurações** no aplicativo
2. Ajuste os seguintes parâmetros:
   - **Sensibilidade**: 50-70% (para iniciantes)
   - **Confiança**: 70-80% (para precisão)
   - **Mostrar alvos**: Ativado (para aprendizado)
   - **Mostrar crosshair**: Ativado

### 4. Download do Modelo IA
1. Na primeira ativação do aimbot:
2. O aplicativo baixará automaticamente o modelo TensorFlow Lite
3. Aguarde o download completar (pode levar alguns minutos)
4. Uma notificação confirmará o sucesso

## 🎮 Primeiro Uso

### 1. Teste do Overlay
1. Toque em **Iniciar Overlay**
2. Um painel flutuante deve aparecer
3. Teste os botões: AIM, SET, REC
4. Se funcionou, toque em **Parar Overlay**

### 2. Teste de Gravação
1. Toque em **Iniciar Gravação**
2. Conceda permissão de captura se solicitado
3. Grave por alguns segundos
4. Toque em **Parar Gravação**
5. Verifique se o arquivo foi salvo

### 3. Teste no Free Fire
1. **Inicie o overlay** no Free Fire Aimbot
2. **Abra o Free Fire**
3. **Entre em uma partida de treino**
4. **Ative o aimbot** usando o botão AIM
5. **Observe** se os alvos são detectados
6. **Ajuste** as configurações conforme necessário

## 🔧 Solução de Problemas

### Overlay não aparece
**Problema**: O painel flutuante não é exibido
**Soluções**:
1. Verifique se a permissão de overlay foi concedida
2. Reinicie o aplicativo
3. Verifique se há outros overlays ativos
4. Teste em uma tela simples primeiro

### Shizuku não conecta
**Problema**: Erro ao conectar com Shizuku
**Soluções**:
1. Verifique se o Shizuku está instalado
2. Reinicie o serviço Shizuku:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
3. Verifique se a depuração USB está ativa
4. Tente reiniciar o dispositivo

### Aimbot não detecta alvos
**Problema**: Nenhum alvo é detectado no jogo
**Soluções**:
1. Verifique se o modelo foi baixado completamente
2. Ajuste a configuração de confiança (diminua para 50-60%)
3. Ative "Mostrar alvos" para debug visual
4. Teste em diferentes cenários do jogo
5. Verifique se há luz suficiente na tela

### Gravação falha
**Problema**: Erro ao iniciar gravação de tela
**Soluções**:
1. Conceda permissão de captura de tela novamente
2. Verifique espaço de armazenamento disponível
3. Feche outros aplicativos que usam câmera/gravação
4. Reinicie o dispositivo se necessário

### Performance baixa
**Problema**: Aplicativo lento ou travando
**Soluções**:
1. Feche outros aplicativos em segundo plano
2. Reduza a qualidade de gravação
3. Diminua a sensibilidade do aimbot
4. Verifique se o dispositivo não está superaquecendo
5. Reinicie o dispositivo

## 📊 Verificação da Instalação

### Lista de Verificação
- [ ] Shizuku instalado e rodando
- [ ] Free Fire Aimbot instalado
- [ ] Permissão de overlay concedida
- [ ] Permissão de captura de tela concedida
- [ ] Permissões Shizuku concedidas
- [ ] Modelo TensorFlow baixado
- [ ] Overlay funciona corretamente
- [ ] Gravação funciona corretamente
- [ ] Aimbot detecta alvos em teste

### Comandos de Verificação
```bash
# Verificar se Shizuku está rodando
adb shell pgrep -f shizuku

# Verificar logs do aplicativo
adb logcat | grep "FreeFireAimbot"

# Verificar permissões concedidas
adb shell dumpsys package com.example.freefireaimbot | grep permission
```

## 🔄 Atualizações

### Atualizar o Aplicativo
1. Baixe a nova versão do APK
2. Instale sobre a versão existente
3. As configurações serão preservadas
4. Reinicie o aplicativo

### Atualizar Shizuku
1. Baixe a nova versão do Shizuku
2. Instale normalmente
3. Reinicie o serviço Shizuku
4. Reconecte no Free Fire Aimbot

## 🆘 Suporte

Se você encontrar problemas não cobertos neste guia:

1. **Verifique os logs**:
   ```bash
   adb logcat | grep -E "(FreeFireAimbot|Shizuku|TensorFlow)"
   ```

2. **Reporte o problema**:
   - Abra uma issue no GitHub
   - Inclua modelo do dispositivo
   - Inclua versão do Android
   - Inclua logs relevantes
   - Descreva os passos para reproduzir

3. **Recursos adicionais**:
   - README.md - Documentação geral
   - Wiki do projeto - Guias avançados
   - Issues do GitHub - Problemas conhecidos

## ⚠️ Avisos Importantes

1. **Uso Responsável**: Este aplicativo é para fins educacionais
2. **Termos de Serviço**: Pode violar ToS de jogos online
3. **Banimentos**: Uso pode resultar em ban da conta
4. **Legalidade**: Verifique leis locais sobre modificação de jogos
5. **Privacidade**: Nenhum dado é enviado para servidores externos

---

**Instalação concluída com sucesso!** 🎉

Agora você pode usar o Free Fire Aimbot. Lembre-se de usar com responsabilidade e apenas para fins educacionais.

