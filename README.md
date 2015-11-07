# Anatomy
anatomy teaching app

Výber programovacieho jazyka:
Natívnym jazykom pre tvorbu Android aplikacii je Java, v ktorej je naprogramovaná aj táto aplikácia.
Ďalšou možnosťou, ktorá stojí za zváženie, je C# , u ktorého je ale problém s nedostatočnou podporou 
(ešte zďaleka nie je takrozšírený ako Java). Ďalším problémom je Xamarin framework, ktorý slúži na 
cross-platform development, avšak na tento nástroj sa nám nepodarilo získať študentskú licenciu.
IDE: Android Studio
Verzovací systém: GitHub

Spracovanie vektorovej grafiky:
S vektorovou grafikou pracujeme za pomoci tried Canvas a Path z Android API.
Pre prácu s svg a jeho konverziu na android path používame túto knižnicu:
https://code.google.com/p/svg-android/wiki/Tutorial

Ovládanie:
Približovanie je implementované dvomi spôsobmi: 1. Pinch to zoom (vzďalovanie dvoch prstov od seba)
http://vivin.net/2011/12/04/implementing-pinch-zoom-and-pandrag-in-an-android-view-on-the-canvas/
https://androidcookbook.com/Recipe.seam?recipeId=2273
http://www.zdnet.com/article/how-to-use-multi-touch-in-android-2-part-3-understanding-touch-events/?tag=content;siu-container
																								2. Tap (pri kliknutí na určité miesto sa obraz automaticky 5x priblíži.
Zvolenie určitej časti tela: okolo zvolenej časti sa zobrazia tlačítka, ktorými sa bude dať spresniť výber.

Načítanie obrázkov z internetu:
Na získanie dát využívame prácu s HTTP requests.
http://rest.elkstein.org/2008/02/using-rest-in-java.html
Zaujímavosťou je, že android zakazuje prácu s Internetom v hlavnom vlákne.
http://stackoverflow.com/questions/6343166/android-os-networkonmainthreadexception
Na prácu s Internetom tiež bolo nutné pridať oprávnenie pre danú aplikaciu do AndroidManiest.xml.
<uses-permission android:name="android.permission.INTERNET"/>

