# Free Fire Aimbot

Um aplicativo Android avançado que implementa um sistema de aimbot para Free Fire usando TensorFlow Lite, permissões Shizuku e funcionalidades de overlay e gravação de tela.

## ⚠️ Aviso Legal

Este projeto é desenvolvido **apenas para fins educacionais** e demonstração de tecnologias Android avançadas. O uso deste aplicativo em jogos online pode violar os termos de serviço e resultar em banimento da conta. Use por sua própria conta e risco.

## 🚀 Funcionalidades

### Core Features
- **Aimbot com IA**: Detecção de alvos usando TensorFlow Lite
- **Overlay Flutuante**: Painel de controle sobreposto ao jogo
- **Gravação de Tela**: Captura de gameplay em alta qualidade
- **Permissões Shizuku**: Acesso a funcionalidades privilegiadas do sistema

### Características Técnicas
- Detecção de objetos em tempo real
- Interface de usuário responsiva
- Configurações personalizáveis
- Otimização de performance
- Suporte a múltiplas resoluções

## 📋 Pré-requisitos

### Software Necessário
- Android Studio 4.0+
- Android SDK API 21+ (Android 5.0+)
- Java Development Kit (JDK) 8+
- Gradle 7.0+

### Dependências do Dispositivo
- Android 5.0+ (API 21+)
- Permissão de overlay do sistema
- Aplicativo Shizuku instalado e configurado
- Pelo menos 2GB de RAM
- Processador com suporte a operações de ponto flutuante

## 🛠️ Instalação

### 1. Configuração do Ambiente
```bash
# Clone o repositório
git clone https://github.com/seu-usuario/freefireaimbot.git
cd freefireaimbot

# Abra no Android Studio
# File > Open > Selecione a pasta FreeFireAimbot
```

### 2. Configuração do Android SDK
```bash
# Configure as variáveis de ambiente
export ANDROID_HOME=/path/to/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
export PATH=$PATH:$ANDROID_HOME/platform-tools

# Instale as dependências necessárias
sdkmanager "platform-tools" "platforms;android-34"
```

### 3. Build do Projeto
```bash
# No diretório do projeto
./gradlew assembleDebug

# Para build de release
./gradlew assembleRelease
```

## 📱 Configuração no Dispositivo

### 1. Instalação do Shizuku
1. Baixe e instale o [Shizuku](https://github.com/RikkaApps/Shizuku)
2. Ative o modo desenvolvedor no Android
3. Ative a depuração USB
4. Execute o Shizuku via ADB:
```bash
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
```

### 2. Permissões Necessárias
- Overlay do sistema (SYSTEM_ALERT_WINDOW)
- Captura de tela (MediaProjection)
- Acesso ao armazenamento
- Permissões Shizuku

### 3. Configuração Inicial
1. Abra o aplicativo Free Fire Aimbot
2. Vá para Configurações
3. Conceda todas as permissões necessárias
4. Configure os parâmetros do aimbot
5. Teste o overlay antes de usar no jogo

## 🎮 Como Usar

### Iniciando o Aimbot
1. **Configuração Inicial**:
   - Abra o aplicativo
   - Configure as permissões
   - Ajuste a sensibilidade e confiança

2. **Ativação do Overlay**:
   - Toque em "Iniciar Overlay"
   - O painel flutuante aparecerá na tela

3. **Uso no Jogo**:
   - Abra o Free Fire
   - Use o painel flutuante para controlar o aimbot
   - Toque em "AIM" para ativar/desativar
   - Use "REC" para gravar gameplay

### Controles do Overlay
- **AIM**: Liga/desliga o aimbot
- **SET**: Abre as configurações
- **REC**: Inicia/para a gravação de tela

### Configurações Avançadas
- **Sensibilidade**: Velocidade de movimento do aimbot (0-100%)
- **Confiança**: Precisão mínima para detecção (0-100%)
- **Alvos Visuais**: Mostrar/ocultar caixas de detecção
- **Crosshair**: Mostrar/ocultar mira central

## 🔧 Arquitetura do Projeto

### Componentes Principais

#### 1. AimbotManager
- Gerencia o modelo TensorFlow Lite
- Processa frames da tela
- Detecta alvos em tempo real
- Calcula movimentos de mira

#### 2. OverlayService
- Cria e gerencia o overlay flutuante
- Interface de controle do usuário
- Comunicação com outros componentes

#### 3. ScreenRecorder
- Captura de tela usando MediaProjection
- Gravação de vídeo em alta qualidade
- Gerenciamento de arquivos de saída

#### 4. ShizukuHelper
- Interface com a API Shizuku
- Execução de comandos privilegiados
- Otimizações de sistema

#### 5. TargetOverlay
- Visualização de alvos detectados
- Crosshair e indicadores visuais
- Feedback em tempo real

### Fluxo de Dados
```
Captura de Tela → TensorFlow Lite → Detecção de Alvos → Overlay Visual
                                                    ↓
Configurações ← Interface do Usuário ← Controles do Overlay
```

## 🎯 Configurações Recomendadas

### Para Melhor Performance
- **Sensibilidade**: 60-80%
- **Confiança**: 70-85%
- **Qualidade de Vídeo**: Média (5 Mbps)
- **FPS**: 30 fps

### Para Máxima Precisão
- **Sensibilidade**: 40-60%
- **Confiança**: 85-95%
- **Mostrar Alvos**: Habilitado
- **Crosshair**: Habilitado

## 🐛 Solução de Problemas

### Problemas Comuns

#### Overlay não aparece
- Verifique se a permissão de overlay foi concedida
- Reinicie o aplicativo
- Verifique se há outros overlays ativos

#### Aimbot não funciona
- Confirme se o modelo TensorFlow foi baixado
- Verifique as permissões Shizuku
- Ajuste a configuração de confiança

#### Gravação falha
- Conceda permissão de captura de tela
- Verifique espaço de armazenamento
- Reinicie o serviço de gravação

#### Performance baixa
- Reduza a qualidade de vídeo
- Feche outros aplicativos
- Use configurações de baixa latência

### Logs de Debug
```bash
# Visualizar logs do aplicativo
adb logcat | grep "FreeFireAimbot"

# Logs específicos do aimbot
adb logcat | grep "AimbotManager"

# Logs do Shizuku
adb logcat | grep "Shizuku"
```

## 📊 Performance e Otimização

### Requisitos de Sistema
- **CPU**: Snapdragon 660+ ou equivalente
- **RAM**: 4GB+ recomendado
- **GPU**: Adreno 512+ ou Mali-G71+
- **Armazenamento**: 100MB+ livres

### Otimizações Implementadas
- Processamento assíncrono de frames
- Cache de modelos TensorFlow
- Gerenciamento eficiente de memória
- Redução de operações de I/O

### Monitoramento
- Contador de FPS em tempo real
- Uso de CPU e memória
- Latência de detecção
- Taxa de acerto do aimbot

## 🔒 Segurança e Privacidade

### Medidas de Segurança
- Nenhum dado é enviado para servidores externos
- Processamento local de imagens
- Criptografia de configurações sensíveis
- Verificação de integridade do modelo

### Privacidade
- Gravações ficam apenas no dispositivo
- Nenhuma coleta de dados pessoais
- Permissões mínimas necessárias
- Código fonte aberto para auditoria

## 🤝 Contribuição

### Como Contribuir
1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

### Diretrizes
- Siga as convenções de código Java/Android
- Adicione testes para novas funcionalidades
- Documente mudanças significativas
- Mantenha compatibilidade com versões anteriores

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

## ⚖️ Disclaimer

**IMPORTANTE**: Este software é fornecido "como está", sem garantias de qualquer tipo. O uso deste aplicativo em jogos online pode violar os termos de serviço e resultar em consequências como banimento de conta. Os desenvolvedores não se responsabilizam por qualquer uso indevido ou consequências decorrentes do uso deste software.

## 📞 Suporte

Para suporte e dúvidas:
- Abra uma [Issue](https://github.com/seu-usuario/freefireaimbot/issues)
- Consulte a [Wiki](https://github.com/seu-usuario/freefireaimbot/wiki)
- Entre em contato via [email](mailto:suporte@exemplo.com)

## 🙏 Agradecimentos

- [TensorFlow Lite](https://www.tensorflow.org/lite) - Framework de IA
- [Shizuku](https://github.com/RikkaApps/Shizuku) - API de permissões privilegiadas
- [Android Open Source Project](https://source.android.com/) - Plataforma base
- Comunidade de desenvolvedores Android

---

**Desenvolvido com ❤️ para fins educacionais**

