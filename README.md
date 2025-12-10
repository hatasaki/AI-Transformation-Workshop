# AI-Transformation-Workshop

## ワークショップ概要

このワークショップでは、以下の 3 つのアプリケーションを連携させて、出張申請支援 AI エージェントシステムを構築します。

- 既存 Java アプリ 1: 出張申請アプリ (`biz-travel-system/travel-request-app`)
- 既存 Java アプリ 2: ホテル検索アプリ (`biz-travel-system/hotel-search-app`)
- AI エージェントアプリ: Microsoft Agent Framework を用いて Microsoft Foundry（旧 Azure AI Foundry）の生成 AI モデルを呼び出す Python アプリ (`travel-agent-app`)

参加者は、上記 3 アプリケーションをローカル環境で実行し、出張申請フローの中で AI エージェントを活用できる一連のシナリオを体験します[。](https://livesend.microsoft.com/i/W4kGb0D5wE2PKYUcewYHnpA5B7U2bJzZ3QgmSA2Qs1KcWxi1VI99YJml6fXfrWtbGek2DbuT5GHJi9qUYHn199JOTwXNlRkCMFcMyVMZcxBi___8H6iRIgV2kLHw2UfN9W)

前提条件として、Microsoft Foundry リソースとプロジェクト、および使用する生成 AI モデルのデプロイは完了済みであることを想定します（モデルのデプロイ方法や Foundry プロジェクトの作成方法については、Azure AI Foundry の公式ドキュメントを参照してください）。

## 前提環境

- OS: Linux（本リポジトリの Dev Container を想定）
- Java 実行環境: JDK がインストール済みであること
- Node/NPM: npm が利用可能であること
- Python 3 と `pip` が利用可能であること (Python 3.10 以上推奨)
- Azure サブスクリプションと Microsoft Foundryプロジェクトが利用可能であること
- Microsoft Foundry 上に対象モデル（例: `gpt-4.1` 等）がデプロイ済みであること

Java アプリケーションはローカルの JDK（本環境では Dev Container にインストール済みの JDK）で実行します。AI エージェントアプリは Python で実装されており、Microsoft Agent Framework と Microsoft Foundry 上のモデルを組み合わせて動作します。

## 手順概要

1. リポジトリのクローンと Dev Container / 開発環境の準備
2. 出張申請アプリ（Java / Spring Boot）の起動
3. ホテル検索アプリ（Java / Spring Boot）の起動
4. 出張申請アプリ UI への接続確認
5. Microsoft Foundry（Azure AI Foundry）の確認（事前準備済みであることの確認）
6. AI エージェントアプリ用の Python 環境構築
7. Microsoft Agent Framework と Foundry モデル利用のための環境変数設定
8. AI エージェントアプリの起動と動作確認
9. AI エージェントを用いた出張申請シナリオの実行

以下で各ステップの詳細を説明します。

## 1. リポジトリのクローン

ローカル環境または Dev Container 内で、GitHub から本リポジトリをクローンします。

```bash
git clone <このリポジトリの URL>
cd AI-Transformation-Workshop
```

VS Code の Dev Container 機能を利用する場合は、リポジトリを開いた後に「Reopen in Container」を実行してください。

## 2. 出張申請アプリ（travel-request-app）の起動

`biz-travel-system/travel-request-app` は Spring Boot ベースの Java アプリです。Maven Wrapper または Maven を用いて起動します。

```bash
cd biz-travel-system/travel-request-app
mvn spring-boot:run
```

デフォルトでは、アプリケーションは `http://localhost:8080` で起動する想定です（実際のポート番号は `application.properties` を参照してください）。

## 3. ホテル検索アプリ（hotel-search-app）の起動

別ターミナルを開き、ホテル検索アプリを起動します。

```bash
cd biz-travel-system/hotel-search-app
mvn spring-boot:run
```

こちらもデフォルトでは `http://localhost:8081` など別ポートで待ち受ける想定です（詳細は `application.properties` を参照してください）。

## 4. 出張申請アプリ UI への接続

ブラウザで出張申請アプリの UI にアクセスし、画面が正常に表示されることを確認します。

- 出張申請一覧・登録画面へのアクセス
- 登録済みデータの表示確認

この段階では、まだ AI エージェントとの連携は行われていません。純粋な Web アプリとしての挙動を確認するステップです。

## 5. Microsoft Foundryの事前準備確認

AI エージェントアプリは、Microsoft Agent Framework 経由で Microsoft Foundryのモデルを呼び出します。

本ワークショップでは、以下がすでに完了していることを前提とします。

- Azure サブスクリプションが作成済み
- Microsoft Foundryのリソースとプロジェクトが作成済み
- Foundry プロジェクト内で利用するモデル（例: `gpt-4.1` 等）がデプロイ済み

これらの手順については、以下の Microsoft 公式ドキュメントを参照してください。

- Azure AI Foundry の概要・プロジェクト作成方法
- Foundry Models のデプロイ方法
- Microsoft Foundry Agents / Models を利用したエージェント構築方法

## 6. AI エージェントアプリ用の Python 環境構築

`travel-agent-app` ディレクトリには、Microsoft Agent Framework を利用した Python ベースの AI エージェントアプリが含まれています。

まず依存パッケージをインストールします。

```bash
cd travel-agent-app
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Microsoft Agent Framework および Azure AI 関連パッケージの利用方法については、以下の Microsoft 公式ドキュメントを参照してください（`agent-framework` および Microsoft Foundry / Azure OpenAI 関連のガイド）。

## 7. 環境変数の設定（Microsoft Foundry / Agent Framework 用）

AI エージェントアプリが Microsoft Foundry 上のモデルを呼び出すためには、エンドポイントやモデルデプロイ名などを環境変数として設定する必要があります。具体的な名前や値は、実際のアプリ実装および Foundry プロジェクトに応じて調整してください。

例（Microsoft Foundry Agents の公式ドキュメントに基づく一般的な設定例）:

```bash
export AZURE_AI_PROJECT_ENDPOINT="https://<your-project>.services.ai.azure.com/api/projects/<project-id>"
export AZURE_AI_MODEL_DEPLOYMENT_NAME="gpt-4.1"
```

加えて、認証情報（Azure CLI / マネージド ID / 接続文字列など）を利用して Microsoft Foundry にアクセスできるようにします。Azure CLI を利用する場合は、事前に `az login` を実行しておきます。

> 参考: Microsoft Foundry Agents (Python) の公式ドキュメントには、環境変数の設定方法や `AzureAIAgentClient` の構成方法が記載されています。

## 8. AI エージェントアプリの起動

環境変数と Python 依存パッケージの準備ができたら、AI エージェントアプリを起動します。

```bash
cd travel-agent-app
source .venv/bin/activate
python app.py
```

アプリケーションはローカルホスト上で HTTP サーバーとして起動し、出張申請アプリなどからのリクエストを受けて、Microsoft Foundry 上のモデルを呼び出すエージェントとして動作します（具体的なポート番号やパスは `app.py` の実装を参照してください）。

## 9. AI エージェントを用いた出張申請シナリオの実行

すべてのコンポーネントが起動したら、ブラウザから出張申請アプリにアクセスし、AI エージェントを用いたシナリオを実行します。

典型的な手順の一例:

1. 出張申請アプリの画面を開く
2. 出張の目的地や日付、予算などを入力
3. 「AI に提案を依頼」など、AI エージェント連携用のボタンや操作を実行
4. AI エージェントが Microsoft Foundry 上のモデルを利用して、候補ホテルや旅程、申請内容案などを生成
5. 生成結果を確認し、必要に応じて修正して申請を確定

このプロセスを通じて、参加者は以下を学びます。

- 既存の業務アプリケーション（出張申請・ホテル検索）に対して、AI エージェントを後付けで連携させる方法
- Microsoft Agent Framework と Microsoft Foundry（Azure AI Foundry）モデルを組み合わせたエージェント構築パターン
- 環境変数や認証設定など、クラウド上の生成 AI サービスを安全に利用するための基本的な設計

以上が本ワークショップの概要および実施手順です。