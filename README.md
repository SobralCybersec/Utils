<div align="center">
  
<img src="https://i.imgur.com/4ItlCqO.png">

  
<h1 align="center">
  <img src="https://cdn-icons-png.flaticon.com/512/716/716784.png" width="30"/>
  Utilidades Diárias:
</h1>

- Organizador automatico de arquivos com IA
- Interface grafica moderna com Java Swing
- Suporte a multiplos modos de organizacao
- Integracao com LLMs (OpenAI, Gemini, gpt4free)

---

<h1 align="center">
  <img src="https://media.giphy.com/media/WUlplcMpOCEmTGBtBW/giphy.gif" width="50"/>
  Navigation | Navegacao
</h1>

###  [Português](#português) | [English](#english)

</div>

---

<h1 align="center">
  <img src="https://cdn-icons-png.flaticon.com/512/2666/2666505.png" width="30"/> Features
</h1>

- **6 Modos de Organizacao**: Inteligente (IA), Tipo, Extensao, Alfabetica, Data, Tamanho
- **Analise Inteligente**: Detecta padroes, contexto e categoriza automaticamente
- **Integracao LLM**: OpenAI GPT, Google Gemini, gpt4free
- **Interface Moderna**: Java Swing com fonte e background customizaveis
- **Log em Tempo Real**: Acompanhe todas as operacoes
- **Seguranca**: API Keys armazenadas em .env
- **Multiplataforma**: Windows, Linux, macOS
- **Standalone JAR**: Sem instalacao necessaria

---

<h1 align="center">
  <img src="https://cdn-icons-png.flaticon.com/512/1705/1705312.png" width="25"/> Tech Stack:
</h1>
<p align="center">
  <img src="https://go-skill-icons.vercel.app/api/icons?i=java,maven,chatgpt,gemini&size=64" />
</p>

--- 

- Java 21
- Maven 3.6+
- Java Swing
- HTTP Client (OpenAI/Gemini APIs)

---

## Português

### Requisitos

- Java 21 ou superior
- Maven 3.6 ou superior

### Modos de Organizacao

1. **Organizacao Inteligente (IA)** - Analisa contexto e padroes
2. **Por Tipo de Arquivo** - Agrupa por categoria (imagens, videos, etc)
3. **Por Extensao** - Cria pasta para cada extensao
4. **Ordem Alfabetica** - Organiza de A-Z
5. **Por Data** - Agrupa por periodo de modificacao
6. **Por Tamanho** - Separa por tamanho do arquivo

### Organizacao Inteligente (IA)

**Modo Local (Sem IA)**

O modo inteligente analisa:
- Padroes de nome - Detecta projetos, backups, downloads
- Contexto - Identifica documentos de trabalho, midia pessoal
- Data - Separa arquivos recentes de antigos
- Tamanho - Identifica arquivos grandes
- Tipo - Reconhece instaladores, logs, configuracoes

**Modo IA (Com LLM)**

Integracao com modelos de linguagem:
- **OpenAI (GPT-3.5/4)** - Mais preciso, requer API key paga
- **Google Gemini** - Gratuito com limites, excelente para uso pessoal
- **Customizado (gpt4free)** - Use APIs gratuitas ou endpoints locais

**Configuracao Segura**

As API Keys sao armazenadas em arquivo `.env` para seguranca.

**Metodo 1: Interface Grafica (Facil)**
1. Clique no botao "Config IA" na interface
2. Selecione o provedor desejado
3. Insira a API Key
4. Clique em "Salvar"

**Metodo 2: Arquivo .env (Manual)**
1. Copie `.env.example` para `.env`
2. Configure suas chaves:
```properties
AI_PROVIDER=GEMINI
GEMINI_API_KEY=sua_chave_aqui
```
3. Reinicie o programa

**Obter API Keys:**
- **Gemini (Gratis):** https://makersuite.google.com/app/apikey
- **OpenAI (Pago):** https://platform.openai.com/api-keys

**Como usar gpt4free:**

```bash
git clone https://github.com/xtekky/gpt4free
cd gpt4free
python -m g4f.api
```

Configure no .env:
```properties
AI_PROVIDER=CUSTOM
CUSTOM_ENDPOINT=http://localhost:1337/v1/chat/completions
```

**Categorias Inteligentes:**

- Projetos
- Documentos de Trabalho
- Midia Pessoal
- Downloads
- Backups
- Configuracoes
- Logs
- Instaladores
- Arquivos Recentes
- Arquivos Antigos
- Arquivos Grandes
- Arquivos Pequenos

### Tipos de Arquivo Suportados

- **IMAGES** (jpg, png, gif, bmp, webp, svg, ico, raw, tiff)
- **VIDEOS** (mp4, mkv, avi, mov, wmv, flv, webm, mpg)
- **DOCUMENTS** (pdf, doc, docx, xls, xlsx, ppt, pptx, txt, csv, rtf)
- **CODE** (java, py, js, html, css, cpp, c, php, json, xml, sql, sh, bat)
- **ARCHIVES** (zip, rar, 7z, tar, gz, bz2, iso)
- **AUDIO** (mp3, wav, flac, aac, ogg, m4a)
- **EXECUTABLES** (exe, msi, dll, so, app, deb, rpm)
- **NETWORK** (pcap, pcapng, cap, dmp)

### Customizacao

**Fonte Customizada**

1. Baixe uma fonte .ttf (ex: Google Fonts)
2. Coloque em: `src/main/resources/fonts/custom.ttf`
3. Recompile: `mvn clean package`

**Background Customizado**

1. Crie/baixe uma imagem PNG (1920x1080)
2. Coloque em: `src/main/resources/images/background.png`
3. Recompile: `mvn clean package`


### Desenvolvimento

```bash
# Compilar
mvn clean install

# Executar
java -jar target/FileOrganizer.jar

# Limpar
mvn clean
```

### Seguranca

- **API Keys**: Armazenadas em .env (nunca commitar)
- **Configuracao**: Interface grafica ou manual via .env
- **Suporte**: OpenAI, Gemini, endpoints customizados

---

## English

### Requirements

- Java 21 or higher
- Maven 3.6 or higher

### Organization Modes

1. **Intelligent Organization (AI)** - Analyzes context and patterns
2. **By File Type** - Groups by category (images, videos, etc)
3. **By Extension** - Creates folder for each extension
4. **Alphabetical Order** - Organizes A-Z
5. **By Date** - Groups by modification period
6. **By Size** - Separates by file size

### Intelligent Organization (AI)

**Local Mode (No AI)**

Intelligent mode analyzes:
- Name patterns - Detects projects, backups, downloads
- Context - Identifies work documents, personal media
- Date - Separates recent from old files
- Size - Identifies large files
- Type - Recognizes installers, logs, configs

**AI Mode (With LLM)**

Integration with language models:
- **OpenAI (GPT-3.5/4)** - More accurate, requires paid API key
- **Google Gemini** - Free with limits, excellent for personal use
- **Custom (gpt4free)** - Use free APIs or local endpoints

**Secure Configuration**

API Keys are stored in `.env` file for security.

**Method 1: Graphical Interface (Easy)**
1. Click "Config IA" button in interface
2. Select desired provider
3. Insert API Key
4. Click "Save"

**Method 2: .env File (Manual)**
1. Copy `.env.example` to `.env`
2. Configure your keys:
```properties
AI_PROVIDER=GEMINI
GEMINI_API_KEY=your_key_here
```
3. Restart program

**Get API Keys:**
- **Gemini (Free):** https://makersuite.google.com/app/apikey
- **OpenAI (Paid):** https://platform.openai.com/api-keys

**How to use gpt4free:**

```bash
git clone https://github.com/xtekky/gpt4free
cd gpt4free
python -m g4f.api
```

Configure in .env:
```properties
AI_PROVIDER=CUSTOM
CUSTOM_ENDPOINT=http://localhost:1337/v1/chat/completions
```

**Intelligent Categories:**

- Projects
- Work Documents
- Personal Media
- Downloads
- Backups
- Configurations
- Logs
- Installers
- Recent Files
- Old Files
- Large Files
- Small Files

### Supported File Types

- **IMAGES** (jpg, png, gif, bmp, webp, svg, ico, raw, tiff)
- **VIDEOS** (mp4, mkv, avi, mov, wmv, flv, webm, mpg)
- **DOCUMENTS** (pdf, doc, docx, xls, xlsx, ppt, pptx, txt, csv, rtf)
- **CODE** (java, py, js, html, css, cpp, c, php, json, xml, sql, sh, bat)
- **ARCHIVES** (zip, rar, 7z, tar, gz, bz2, iso)
- **AUDIO** (mp3, wav, flac, aac, ogg, m4a)
- **EXECUTABLES** (exe, msi, dll, so, app, deb, rpm)
- **NETWORK** (pcap, pcapng, cap, dmp)

### Customization

**Custom Font**

1. Download a .ttf font (ex: Google Fonts)
2. Place in: `src/main/resources/fonts/custom.ttf`
3. Recompile: `mvn clean package`

**Custom Background**

1. Create/download a PNG image (1920x1080)
2. Place in: `src/main/resources/images/background.png`
3. Recompile: `mvn clean package`

### Development

```bash
# Build
mvn clean install

# Run
java -jar target/FileOrganizer.jar

# Clean
mvn clean
```

### Security

- **API Keys**: Stored in .env (never commit)
- **Configuration**: Graphical interface or manual via .env
- **Support**: OpenAI, Gemini, custom endpoints

### Notes

- Generated JAR is standalone and contains all dependencies
- Application does not require installation
- Compatible with Windows, Linux and macOS
- Intelligent organization uses pattern analysis (no internet required)
- Code follows SOLID and Clean Code principles
- No emojis in code
- No redundant comments
- Minimized prints
