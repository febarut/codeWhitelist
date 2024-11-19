## CodeWhitelist - Whitelist Eklentisi 🚀

**CodeWhitelist**, Minecraft sunucunuz için doğrulama kodu tabanlı bir giriş kontrol sistemi sağlar. Sunucuya katılan oyuncular, kendilerine sunulan bir doğrulama kodunu girerek erişim alabilir. Bu sistem, oyuncuların doğrulama yapmadan sunucuda hareket etmesini veya herhangi bir işlem yapmasını engeller.

### ✨ Özellikler
- **Doğrulama Kodu Sistemi**: Sunucu her açıldığında bir doğrulama kodu oluşturulur ve sunucunun `config.yml` dosyasına kaydedilir.
- **IP Tabanlı Kayıt**: Oyuncular, doğrulama yaptıktan sonra IP adresleriyle birlikte kayıt edilir. Böylece bir sonraki girişlerinde tekrar kod girmelerine gerek kalmaz.
- - **Freeze Sistemi**: Doğrulama yapılmadan önce oyuncuların hareket etmesi, eşya kullanması, hasar vermesi veya alması engellenir.
- **Kod Gösterimi**: Doğrulama kodu, sunucu tamamen oyuncuların giriş yapabilir duruma geldiğinde konsola yazdırılır.
- **Esnek Yönetim**:
  - `config.yml` üzerinden IP kontrolü açılıp kapatılabilir.
  - Oyuncu doğrulama listesi kolayca düzenlenebilir.

### 🛠 Kurulum
1. Eklentiyi indirip sunucunuzun `plugins` klasörüne yerleştirin.
2. Sunucunuzu yeniden başlatın.
3. Sunucu açıldıktan sonrasında konsolda veya `plugins/codeWhitelist/config.yml` dosyasından kodu görebilirsiniz.

### 🔧 Yapılandırma
Eklenti ilk çalıştırıldığında `config.yml` otomatik olarak oluşturulur. Dosya içeriği şu şekilde görünür:

```yaml
server-code: "ABC123" # Doğrulama Kodu
players: [] Oyuncu İsimleri:Ip Adresleri
settings:
  ip-check: true # IP kontrolü (Açık olması önerilir.)
```

- **`server-code`**: Sunucunun oluşturduğu doğrulama kodu.
- **`players`**: Oyuncuların doğrulama yaptıktan sonra kaydedilen isim ve IP adresleri.
- **`ip-check`**: Oyuncunun ip adresi bazlı kontrol sistemini kapatıp açmak için mevcut bir özellik

### 📜 Komutlar
- **`/kod <kod>`**: Oyuncuların doğrulama kodunu girmesini sağlar. Eğer kod doğruysa oyuncunun erişimi açılır.

### 📂 Kaynak Kod
Bu eklentinin kaynak kodu tamamen açık ve özelleştirilebilir. Kendi ihtiyaçlarınıza göre düzenleyebilirsiniz.

### 📧 Destek
Herhangi bir sorun ya da öneriniz varsa lütfen bizimle iletişime geçebilirsiniz!
