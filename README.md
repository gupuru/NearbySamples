# androidの[Nearby API](https://developers.google.com/nearby/messages/overview "NearBy API")のサンプル集

**※綺麗には作っていないです。**

##Nearby APIの有効

Nearby Messages APIを、Google Developers ConsoleでAPIを有効にし、
__アプリ用のクライアントキーを作成する__必要があります。

この辺りの手順は、Google Map APIとかと一緒です。
詳しくは、こちらの[記事](http://qiita.com/chibatching/items/73d4234bb0ac742260c4 "記事")を参考にしてください。

APIキーを作成したら、AndroidManifestの内のここに書いてください。

```
<meta-data
        android:name="com.google.android.nearby.messages.API_KEY"
        android:value="作成したAPIキー" />
```

##サンプル集
以下のものを作っています。

- メッセージ送信
 
 > Edittextに入力したテキストを相手に送信します。
 
- すれ違い通信っぽいもの

 > デバイス名と乱数で作った数字を10秒間隔で送ります。これは、あくまで
 __っぽい__ ものです。

##参考サイト(ありがとうございます^^)
- 公式のサンプル
https://github.com/googlesamples/android-nearby

- Nearby APIのドキュメント
https://developers.google.com/nearby/messages/android/pub-sub

- 公式のサンプルの解説記事
http://teshi04.hatenablog.com/entry/2015/09/21/172029

- 基本事項
http://qiita.com/chibatching/items/73d4234bb0ac742260c4
http://seesaakyoto.seesaa.net/article/427295823.html