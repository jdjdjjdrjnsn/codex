# Android Telefon Temizleyici (MVVM)

Bu proje, Android 8.0+ cihazlarda gereksiz dosyaları tespit edip kullanıcı onayıyla temizlemeye odaklanan modern bir telefon temizleyici uygulamasının başlangıç sürümüdür.

## Uygulanan Özellikler

- **Akıllı tarama**: Cache, geçici dosyalar, boş klasörler ve büyük dosyaları listeler.
- **Risk sınıflandırma**: Öğeleri `SAFE`, `REVIEW`, `RISKY` olarak işaretler.
- **Önizlemeli temizleme**: Dosyalar checkbox ile seçilir, kullanıcı onayı olmadan silme yapılmaz.
- **Tek dokunuşla temizlik**: “Hızlı Temizlik” ile seçili öğeler temizlenir.
- **MVVM yapı**: `CleanerViewModel`, `CleanerRepository`, domain model ayrımı yapılmıştır.
- **Arka plan altyapısı**: WorkManager ile zamanlanabilir tarama worker'ı eklendi.

## Mimari

- `domain/`: model ve iş kuralı sınıfları (`RiskClassifier`)
- `data/`: Android dosya tarama/silme katmanı (`AndroidCleanerRepository`)
- `ui/`: ViewModel ve ekran durumu
- `worker/`: arka plan tarama worker'ı

## Güvenlik Yaklaşımı

- Sistem dizinleri (`/system`, `/vendor`) **riskli** olarak işaretlenir.
- Varsayılan seçili öğeler yalnızca güvenli kategorilerdir.
- Kullanıcı açık onayı olmadan hiçbir dosya silinmez.

## Notlar

- `MANAGE_EXTERNAL_STORAGE` izni yalnızca gereksinim halinde kullanılmalıdır ve Play Store politikalarına göre ayrıca gerekçelendirilmelidir.
- Benzer/duplicate medya analizi için hash tabanlı ek modül, bu temel sürüme opsiyonel geliştirme olarak eklenebilir.
