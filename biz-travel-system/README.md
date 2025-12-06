# Biz Travel System

Spring Boot (Java 17) サンプル構成。`biz-travel-system` 配下に 2 つのアプリがあります。

- `travel-request-app`: 出張申請アプリ。REST API + Thymeleaf UI。ローカルファイル `data/travel-requests.json` に保存。
- `hotel-search-app`: ホテル検索アプリ。REST API で city / maxPrice / feature を受け取り、ローカルファイル `data/hotels.json` を検索。

## 前提
- Java 17
- Maven 3.9 以降

## 実行方法
各アプリのディレクトリで Maven を実行します。

```bash
cd biz-travel-system/travel-request-app
./mvnw spring-boot:run
```

別ターミナルでホテル検索アプリ:

```bash
cd biz-travel-system/hotel-search-app
./mvnw spring-boot:run
```

## エンドポイント

### 出張申請アプリ
- ブラウザ UI: `http://localhost:8080/`
- REST 一覧: `GET /api/travel-requests`
- REST 登録: `POST /api/travel-requests`
  - JSON 例
    ```json
    {
      "title": "出張A",
      "startDate": "2025-01-10",
      "endDate": "2025-01-12",
      "purpose": "顧客訪問",
      "city": "Tokyo",
      "estimatedCost": 120000
    }
    ```

### ホテル検索アプリ
- 検索: `POST /api/hotels/search`
  - リクエスト例
    ```json
    {
      "city": "Tokyo",
      "maxPrice": 15000,
      "feature": "駅に近い"
    }
    ```
  - レスポンス: 条件に合うホテル配列

## データファイル
- `travel-request-app/data/travel-requests.json` (起動時に空配列で初期化)
- `hotel-search-app/data/hotels.json` (起動時にサンプルデータ自動生成)

## 注意
- 簡易実装のため永続化はローカル JSON ファイルのみです。
- 複数同時起動時はポートが競合しないよう必要に応じて `server.port` を変更してください。
