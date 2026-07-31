# Spring Boot + React フルスタックデモ

HttpOnly Cookie ベースのリフレッシュトークン、セッション管理、設定可能なセキュリティ設定を備えた、**プロダクション志向**のフルスタックデモアプリケーションです。安全な認証パターンを実装しています。

## 🚀 主な機能

- **安全な認証**:
  - **HttpOnly Cookie**:リフレッシュトークンを HttpOnly Cookie に保存し、XSS 攻撃を防止します。
  - **セッション管理**:アクセストークンは `sessionStorage`(タブ固有)に保存されます。
  - **ブラックリスト**:ログアウト時に Redis を使ってアクティブなトークンをブラックリスト化します。
- **環境ベースのセキュリティ**:
  - **CORS**:環境(dev / test / prod プロファイル)ごとに設定可能。
  - **Cookie 属性**:`secure` と `SameSite` は環境ごとに異なります。
  - **シークレット**:`JWT_SECRET` は環境変数で管理します。

## 🏗 アーキテクチャ概要

### コンポーネントごとの役割

- **バックエンド(Spring Boot)**:JWT トークンの発行、資格情報の検証、セッション状態の管理、セキュリティポリシーの適用、Redis でのトークンブラックリスト処理を担当します。
- **フロントエンド(React)**:UI 状態を管理し、アクセストークンを sessionStorage に保存、401 エラー時にリフレッシュをトリガーし、ログイン/ログアウトの UI フローを処理します。
- **Redis**:即時失効のためのトークンブラックリストを保持し、アクティブセッションのセッションストアとして機能します。

### トークンの責務分担

- **アクセストークン**:有効期間は短く(1 時間)、API 認証に使用され、XSS アクセスを防ぐために sessionStorage に保存されます。
- **リフレッシュトークン**:有効期間は長く(7 日間)、新しいアクセストークンを取得するために使用され、XSS による窃取を防ぐために HttpOnly Cookie に保存されます。

### なぜ sessionStorage か?

`localStorage` ではなく `sessionStorage` を選んだ理由は、**タブ固有**で**タブを閉じるとクリアされる**ためです。これにより、セッション間のセキュリティ分離が向上し、異なるブラウジングコンテキスト間でのトークン再利用リスクが低減します。また、多くのアプリケーションが期待する「ブラウザを閉じたらログアウト」という挙動も自動的に実現できます。

### なぜブラックリストか?

JWT は本来ステートレスですが、それでも**セッションを直ちに失効させる**機能が必要です(例:ユーザーがログアウトをクリックした、管理者がセッションを強制終了した、セキュリティ侵害を検知したなど)。Redis ベースのブラックリストにより、トークン検証時に O(1) ルックアップが可能となり、「ほぼステートレスな JWT」と「必要に応じた即時失効機能」の両立を実現できます。

## 🛠 技術スタック

- **バックエンド**:Spring Boot 3.x、Spring Security 6、JPA、Redis、MySQL/H2
- **フロントエンド**:React 18、TypeScript、Axios、Context API
- **テスト**:JUnit 5、MockMvc、TestContainers 対応

## 🏃‍♂️ はじめに

### 前提条件
- Java 17 以上
- Node.js 18 以上
- Docker(任意、Redis/MySQL 用)

### バックエンドのセットアップ

1. **環境変数の設定**:
   必要な環境変数を設定します。`JWT_SECRET` は必須で、32 バイト以上である必要があります。
   ```bash
   export JWT_SECRET=your_secure_random_secret_key_at_least_32_bytes
   export CORS_ALLOWED_ORIGINS=http://localhost:3000
   ```
   
   **必須環境変数**:
   - `JWT_SECRET`:安全なランダムシークレットキー(32 バイト以上)
   - `CORS_ALLOWED_ORIGINS`:フロントエンドアプリケーションのオリジン
   
   **任意環境変数**:
   - `JWT_EXPIRATION`:アクセストークンの有効期限(ミリ秒、デフォルト:3600000)
   - `JWT_REFRESH_EXPIRATION`:リフレッシュトークンの有効期限(ミリ秒、デフォルト:86400000)

2. **Maven で実行**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

### フロントエンドのセットアップ

1. **環境変数の設定**:
   `frontend` ディレクトリに `.env` ファイルを作成します:
   ```env
   REACT_APP_API_BASE_URL=http://localhost:8080/api
   ```

2. **インストールと実行**:
   ```bash
   cd frontend
   npm install
   npm start
   ```

## 🔒 セキュリティ実装の詳細

### 認証フロー
1. **ログイン**:クライアントが資格情報を送信します。サーバーは検証し、以下を返します:
   - `accessToken`(JSON ボディ)→ `sessionStorage` に保存。
   - `refreshToken`(HttpOnly Cookie)→ ブラウザが自動的に処理。
2. **アクセス**:クライアントが `Authorization: Bearer <token>` ヘッダーを送信します。
3. **リフレッシュ**:アクセストークンの有効期限が切れると(401)、クライアントは `/refresh` を呼び出します。サーバーは Cookie を検証し、新しいアクセストークンを返します。
4. **ログアウト**:クライアントが `/logout` を呼び出します。サーバーは Cookie を無効化し、Redis でアクセストークンをブラックリスト化します。

### 環境別の Cookie セキュリティ
- **Development**:`secure=false`、`SameSite=Lax`(localhost では HTTP を許可)
- **Test**:`secure=false`、`SameSite=Strict`(CSRF 対策をより厳格に)
- **Production**:`secure=true`、`SameSite=Strict`(HTTPS 必須)

### 現状

#### 実装済み
- リフレッシュトークン用の HttpOnly Cookie
- sessionStorage に保存するアクセストークン
- ログアウト時の Redis ベーストークンブラックリスト
- 環境別の Cookie 属性(secure / SameSite)
- 環境変数による CORS 設定
- 環境変数による必須化された `JWT_SECRET`

#### 今後の改善予定
- リフレッシュごとのトークンローテーション(現在:リフレッシュトークンは 7 日間有効)
- 追加保護のための CSRF トークン統合
- 認証エンドポイントへのレートリミット

### トレードオフと制約
- **CSRF**:ステートレスな JWT フローのため無効化。本番では `SameSite=Strict` で緩和。より高いセキュリティ要件では CSRF トークンの追加を検討。
- **トークンローテーション**:現在、リフレッシュトークンは 7 日間有効。使用ごとにローテーションすると盗難検知が可能になるが、複雑度は上がる。
- **ステートレス性**:JWT はステートレスだが、トークン失効には Redis の状態が必要。
- **セッション結合**:リフレッシュトークンは元のデバイス / セッションに紐付けられる。

## 🧪 テスト

Maven を使って結合テストを実行します:
```bash
./mvnw test
```
テストは分離のため H2 データベースと組み込み Redis 設定を使用します。

## 📝 ライセンス
MIT

---

## 🇬🇧 English | 🇨🇳 中文

- [English version](./README.en.md)
- [中文版本](./README.zh.md)
