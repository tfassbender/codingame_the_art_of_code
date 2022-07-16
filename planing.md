# CG - The Art of Code

## Multiplayer Bot Game für Codingame

### Basics

Das Spiel soll ähnlich wie Rsiko funktionieren:
- Spieler können Einheiten auf der Karte verteilen, die sich gegenseitig bekämpfen um gebiete zu erobern
  - Jeder Spieler erhällt pro Zug eine vorgegebene Anzahl an neuen Einheiten (ca. 5-10)
  - Das spielfeld soll in mehrere (kleine) Regionen aus mehreren Gebieten aufgeteilt werden
    - Für jede der Regionen, die ein Spieler komplett kontrolliert erhällt er zusätzliche Einheiten zu Beginn einer Runde
  - Zusätzliche Einheiten auch für eine bestimmte Anzahl an Feldern vergeben (egal ob sie eine zusammenhängende Region bilden)
    - Fände ich ziemlich cool, weil dann diese strategien kommen wie hau dem gegner noch eine Region weg, damit dieser unter z.B. 12 liegt und dann nur 3 dazu bekommt. 
    - Eine der Bonuseinheiten Regeln könnte mit einer neuen Liga eingeführt werden (z.B. das mit den zusammenhängenden Regionen).
      - Plus damit kommen zusätzliche Abwägungen ins Spiel: versuch ich einen Bonus eher mit vielen Feldern oder mit einer Region?
  - Die Einheiten können beliebig in jedem Gebiet verteilt werden, dass der Spieler kontrolliert
- Die Kämpfe sollen ohne Zufallsprinzip ausgetragen werden:
  - 60% der angreifenden Truppen zerstören eine der verteidigenden Truppen
  - 70% der verteidigenden Truppen zerstören eine der angreifenden Truppen
  ? Wie soll gerundet werden (immer abrunden wäre schlecht, weil einzelne Truppen dann nichts ausrichten können)
    > 4 Ideen (Regel 2 und 3 gefallen mir am besten):
      > 1) Wir runden immer auf oder
      > 2) Wir runden immer ab auf dem Schlachtfeld, sammeln die Stellen nach dem Komma (zwei halbe Einheiten sind eine ganze) und ziehen das beim nächsten Einheiteverteilen von den verfügbaren Einheiten ab.
      > 3) Wie regel zwei, aber mit aufrunden und truppen bonus statt abzug
      > 4) Es gab ein Codingame spiel was diese Abwägung alterniert (einen Zug ja und einen Zug nein), aber das finde ich nicht so elegant
    - Wir haben uns für c) entschieden.
  - Wenn nach einem Angriff keine Verteidiger mehr übrig sind und mindestens ein Angreifer übrig ist, rücken die Angreifer auf das angegriffene Feld vor
  - Falls nach dem Angriff noch Verteidiger oder keine Angreifer mehr übrig sind, bleibt die Kontrolle über die Felder bestehen
    ? Kann man mit nur einer Einheit angreifen und kann man ein land ohne Einheiten zurücklassen?  
      > Ich würde sagen, dass man mit einer einzelnen Einheit angreifen kann.  
    ? Behält man ein Land, wenn keine Einheiten mehr drin stehen oder ist das neutral?  
      > Ich würde sagen, die Gebiete bleiben dann dem Spieler, der als letztes da drin war, auch wenn er alle Einheiten raus gezogen hat. Sonst muss man zu viele Truppen zurück lassen.  
      > Das würde auch eine Strategie ermöglichen, bei der man mit einem Großen Stoßtrupp ein einzelnes Gebiet an der Front durchbricht um dann mit einer kleinen Truppe durch zu kommen, weil hinter der Frontlinie wahrscheinlich keine Verteidigung ist (ich glaube wenn so eine Strategie funktioniert, würde das das Spiel spannender machen)  
      > Das würde noch eine Strategie ermöglichen: Es kann sinnvoll sein zuerst den Gegner angreifen zu lassen, weil die Verteidigung etwas stärker ist als der Angriff. Also macht man zuerst ein paar "dummy"-Züge, indem man die Einheiten im Hinteren bereich der Karte im Kreis laufen lässt. Das geht zwar auch, wenn man immer eine Einheit zurück lassen muss, aber dann kann das jeder immer machen. Wenn die Felder sowiso hallten müsste man das von Anfang an planen und die Strategie würde einen auch ein paar Truppen kosten, die nicht an die Front können.

### Ziel

Wenn ein Spieler alle Gebiete erobert hat, hat er gewonnen  
Wenn nach 200 Zügen (max bei CG) kein Spieler alle Gebiete erobert hat, gewinnt der, der mehr Gebiete hällt  
--Wenn nach 200 Zügen beide Spieler gleich viele Gebiete halten, gewinnt der Spieler der mehr gegnerische Truppen zerstört hat--
> Gefällt mir, offensives Spielen sollte hier mehr belohnt werden (da der Verteidiger eh schon einen Vorteil in Kämpfen hat). Sollte es dann noch keinen unterschied geben, kann das von mir aus ein Draw sein.  
> Ich glaube das mit der Anzahl an zerstörten gegnerischen Truppen sollten wir raus lassen. Sonst könnte man die halbe map erobern und dann nur noch verteidigen. Weil die Verteidigung stärker ist, gewinnt man damit (falls man alle Gebiete verteidigen kann). Vielleicht sollten wir uns einen anderen Tie-Breaker überlegen, aber ich glaube es wird auch nur mit der Anzahl der eroberten Felder gehen. Vermutlich wird sowiso vorher einer vom Feld gefegt.  

### Spielerzahl

Ich würde sagen, wir beschränken es auf 2 Spieler weil:
- CG ein Maximum von 200 Zügen hat (dann bleiben schon nicht mehr so viele Züge übrig, wenn man die auf 2 Spieler mit je 2 Phasen aufteilt)
- Bei mehr als 2 Spielern ergeben sich neue Möglichkeiten bei der Zugreihenfolge, die zu Problemen führen würden (Angriff im Kreis)
- Für 2 Spieler kann man leichter eine Symetrische Karte erstellen
> Oh das macht Sinn, ich wäre dann auch für 2 Spieler, dass macht vieles besser. Ich möchte auch Symmetrische Karten haben.

### Zugreihenfolge

Das Spiel soll in zwei verschiedenen Zug-Phasen gespielt werden:
- In einer Phase werden die neuen Einheiten auf dem Spielfeld verteilt
  - Das können die Spieler gleichzeitig machen, weil das voneinander unabhängig ist (kein Spieler weiß, wo der andere die Truppen einsetzen wird)
- In der zweiten Phase werden alle Bewegungsbefehle (auf einmal) abgeschickt und nacheinander ausgeführt
  - Beide Spieler müssen eine Liste mit Bewegungsbefehlen angeben (ohne zu wissen, welche Bewegungen der andere Spieler machen wird)
  - Die Einheiten können sich nur in ein benachbartes Gebiet bewegen
  - Einheiten die sich in ein Benachbartes Gebiet bewegt haben können von da aus nicht weiter bewegt werden
    - Hier müssen wir aufpassen: Wenn die Einheiten sich in ein Gebiet bewegen, dass dann angegriffen wird, müssen wir das so behandeln, als würden zuerst die Einheiten zerstört, die schon da waren (falls aus diesem Gebiet dann Truppen bewegt werden, können evtl. nicht so viele bewegt werden, wie da sind)
  - Ich fände es sinnvoll, wenn wir zuerst alle Truppen bewegungen durchgehen und dann die Angriffe oder umgekehrt. Ersteres hat den Vorteil, das jeder zug 1 zu 1 so umgesetzt wird, wie der Spieler das angibt. Je nach dem gibt das wohl noch einen Vorteil für den Angreifer oder Verteidiger.
    - Nachdem etwas Zeit vergangen ist finde ich meine Idee doch nicht mehr so gut, aber ich lass die erstmal noch hier, damit du die Siehst.
  - Die Bewegungen werden dann nacheinander ausgeführt:
    - Wenn die Bewegungen unabhängig voneinander sind, wird erst die erste Bewegung des ersten Spielers ausgeführt, dann die erste Bewegung des zweiten Spielers, die zweite Bewegung des ersten Spielers, ...
    - Wenn zwei Felder sich im selben Zug (index in der liste) gegenseitig angreifen, werden die Angriffe gleichzeitig ausgeführt (beide Truppen zerstören 60% der jeweils anderen)
    - Wenn ein Spieler gleichzeitig ein Feld angreift, aus dem der andere Spieler sich heraus bewegt...
      - Könnte man wie oben erwähnt behandeln. Ansonsten würde ich auch zuerst den Angriff ausführen.
      ? ... wird zuerst der Angriff auf das Feld ausgeführt (sonst könnte man zu einfach immer weiter davon laufen)
        > Also ewig könnte man nicht weglaufen und man verliert hierdurch auch möglichkeiten :P (weniger Regionen, die man kontrolliert). Falls eine Einheit zurückbleiben muss verliert man so automatisch auch Truppen. Und für mehr Strategien fände ich das auch nicht so schlecht.
    - Gibt es noch mehr Möglichkeiten, die ich übersehen habe?
      > ich glaube nicht
  - Es gäbe noch eine Sache zu klären für das nacheinander: Wenn z.B. Zug 3 (Angriff) nicht mehr möglich ist, weil die Einheiten vorher vernichtet wurden sind, werden die Züge danach um eins vorgerückt und es gibt einen neuen Zug 3? 
    > Ich währe eher für ein "Passen" an dieser Stelle von dem Spieler.  
      > Guter Punkt. Es kann oft sinnvoll sein seine Angriffe später auszuführen, weil die Verteidigung stärker ist. Daher währe ich auch für ein "Passen" und der Zug fällt dann aus, wegen ... Regen ... :P  
  ? Was passiert, wenn ein Angriff mit 10 Truppen von einem Feld stattfinden soll auf dem sich 15 Truppen befinden, aber vor dem Angriff werden 3 Truppen auf dem Feld zerstört?  
    > Man könnte weiterhin mit 10 Truppen angreifen (oder mit allen, falls es weniger sind als die Anzahl, mit der eigentlich Angegriffen werden sollte)  
      > Wenn wir das machen währe es wichtig, dass man das Feld behällt, auch wenn man keine Einheit mehr in dem Feld hat.  
    > Man könnte den Angriff anteilig Reduzieren: 66% der Truppen sollten eigentlich angreifen (10/15) - jetzt greifen auch 66% an (8/12)  
      > Ich glaube ich finde die erste Lösung besser. Es macht mehr sinn mit vielen Einheiten anzugreifen. Vor allem wenn man das Feld sowiso hällt, wenn es leer ist. Außerdem ergeben sich bei der zweiten Lösung wieder Probleme beim Runden.  

### Karte

Die Karte für das Spiel soll zufällig generiert werden
  - Die Karte soll symetrisch sein, damit das Spiel ausgeglichen ist
  - Auch die Regionen (die zusätzliche Einheiten bringen) sollen symetrisch sein
  - Es soll ein Random Seed verwendet werden (damit man den bei CG angeben kann)
  > Diese Regeln finde ich auch alle sehr gut.
? Gibt es einen Algorithmus um einen solchen Graphen zufällig zu erstellen? Oder schreiben wir den selbst?  
  > Notfalls können wir da auch was zusammenbasteln. Vielleicht ist das zu spezifisch, dadurch das es Regionen gibt, aber mal schauen. Könnte auch spaß machen das zu basteln. Zum testen würde ich erstmal eine handvoll selbsterstellte Maps nehmen.  
    > Gute Idee. Dann können wir das Problem später klähren und erst mal die anderen sachen Testen.  

### Startgebiete

? Wie viele Startgebiete soll ein Spieler bekommen? (ich würde sagen mehrere, damit man etwas schneller startet (wegen der begrenzten Rundenzahl); vielleicht abhängig von der Größe der Karte?)  
  > Ich würde auch sagen mehrere und abhängig von der Karten größe. Mit den Zahlen muss man mal ein wenig testen. Ich dachte auch an sowas wie % von Karte (+- x*random), aber wahrscheinlich ist das ohne Random Sauberer und weniger Komplex. Tendenz ist 3-5 Startfelder.  
? Werden die Startgebiete zugewiesen, oder kann man die selbst wählen?  
  > Ich bin für selbstwählen, dass macht es so viel cooler.  
  - Selbst wählen würde zusätzliche Komplexität in das Spiel bringen und eine Dritte Phase einführen (die nur einmal gemacht wird)  
    - Die Startfelder selbst zu wählen, könnte man ein einer höheren Liga einführen  
      > Das freizuschalten ist ein cooles neues feature  
      - Vorher wird als Antwort nur RANDOM akzeptiert (was in der höheren Liga auch noch möglich sein sollte, damit der Bot nicht beim Liga-Aufstieg kaputt geht)  
        > Ich denke das ist eine gute Idee  
        > Wenn beide Spieler RANDOM antworten, sollten die Startfelder zufällig, aber symetrisch ausgewählt werden
  - Selbst die Startfelder zu wählen könnte dazu beitragen, dass die Spieler weniger symetrisch spielen (was das Spiel spannender machen würde - vorallem zum zugucken)  
    > Absolut!  
  ? Falls man Startgebiete wählen kann: Was passiert, wenn die Spieler die selben Startgebiete wählen?  
    - Nacheinander kann man die nicht wählen, weil das ein unfährer Vorteil für den ersten Spieler währe  
      > Bei einer Symmetrischen Map wäre Spieler 2 bei nacheinander mindestens gleichzeit, wahrscheinlich sogar stärker. Du kannst entweder die Wahl deines Gegners spiegeln (beide sind gleichstark) oder (falls besser) einen Zug machen, der auf den vorherigen Zug reagiert. (Das ist wohl irgendwie dumm bei dem Spiel Counting-Tic-Tac-Toe auf Codingame :D)  
    - Das ist garnicht so unwahrscheinlich, wenn es eindeutig vorteilhafte Felder gibt  
    ? Vielleicht kann man nur in seiner Hälfte der Karte eine Startposition wählen?  
      > Vielleicht müsste man hierdrauf zurückgreifen aber ich fände es cooler, wenn wir mit einer gemixten auswahl umgehen können.  
      > Notfalls muss bei einem Submit ein Seed von beiden seiten Spielen (SP1, SP2) und (SP2, SP1), aber das würde ich gerne vermeiden, falls das geht.  
      > Es gäbe noch eine Reihenfolge wo jeder Spieler mal einen Vorteil hat: Spieler 1 ein fängt mit einem Feld an. Danach ist zweimal Spieler 2 dran. Dann ist zweimal Spieler 1 dran usw. Jeder hat nach seinem Zug ein Feld mehr als der Gegner (bis auf dem Letzten Zug, der ist vorbestimmt durch den anderen)  
        > Wenn wir das so machen, dass man darauf reagieren kann, was der Gegner macht, würde der Start aber lange dauern und das Spiel kann nicht mehr so lange gehen (wegen der 200 Runden Begrenzung)  
      > Es gäbe noch eine weitere möglichkeit wie bei dem Spiel 4 Gewinnt auf Codingame: Nacheinander ziehen, aber Spieler zwei darf einmal die Auswahl vom gegner stehlen. Das macht aber nur Sinn, falls Spieler 2 nicht im Vorteil ist (Asymmetrische Map).  
        > Ich glaube die Symetrische Map aufzugeben macht wenig sinn. Sonst ist das Spiel zu zufällig und könnte durch eine gute Startposition entschieden werden (wie bei Risiko: Wenn du in den ersten paar Runden Australien eroberst, hast du schon so gut wie gewonnen)  
      > Letzte und vielleicht sogar meine lieblingsidee: es gibt eine hälfte wo die Wahl von Spieler 1 bevorzugt wird und eine von Spieler 2. Wenn ein Spieler übergangen wird, hängt der halt länger in der Auswahlphase und du siehst über die Inputs, wie viele du noch Platzieren musst und wie viele der Gegner noch machen muss. Willst du später lieber platzieren, das bevorzugst du die wahl in der "gegnerischen" hälfte und ansonsten eher in deiner eigenen.  
        > Die Idee ist ziemlich cool. Mir macht nur sorgen, dass man damit die Startphase zu lang ziehen könnte (wegen der 200 Züge Begrenzung). Und das ist recht kompliziert, daher sollten wir das vielleicht erst in der zweiten oder dritten Liga einführen. Wir implementieren die mit dem übergehen, aber in den ersten Ligen wird das nicht genutzt. Stattdessen kann dann erstmal nur jeder in seiner hälfe auswählen.  
  ? Falls man Startgebiete wählen kann: Soll man auch auswählen können wie viele Einheiten in ein Startgebiet gestellt werden?  
    > Ne lieber die erste Phase simpel halten und womöglich in dem ersten Zug dann bonustruppen geben (+10 so als Beispiel) oder macht es das zu schwierig auf den Gegner zu reagieren?  
      > Das mit den Bonustruppen finde ich gut. Wenn es schwierig ist auf den Gegner zu reagieren finde ich das eigentlich auch ganz gut :D  
Die Gebiete, die keine Startgebiete sind sollten mit neutralen Einheiten besetzt werden:  
  ? Wie viele Neutrale Einheiten pro Gebiet? Immer gleich viele?  
    - Wenn man die Startposition auswählen kann müssen es entweder überall gleich viele sein, oder man muss vorher wissen wie viele wo sind (sonst kann man beim Start einfach Pech haben)  
      > Man sollte vorher wissen wo wie viele sind denke ich. Die neutralen Einheiten, die quasi auf deiner Auswahl mal waren werden dann gelöscht oder? Oder werden die zu eigenen Einheiten? Dann würde man entweder strategisch gute Position bevorzugen oder Felder mit vielen neutralen Einheiten.  
    - In jedem fall müssen die Symetrisch verteilt werden  
      > Auf jeden fall!  
    - Die meisten Felder sollten 2 Neutrale Einheiten haben denke ich. Vielleicht ein paar mit 1 oder 3, aber nicht mehr als 3. Man soll primär den anderen Bot bekämpfen; nicht die neutralen Einheiten.  
      > Ich bin auch für 1-3, aber die verhältnisse oder wahrscheinlichkeiten muss man denke ich mal testen. Was sich richtig anfühlt hängt auch von den ganzen anderen Regeln und Parametern ab.  

### Grafik

> Ich merke das ich bei den Grafiken noch nicht so viele Ideen/Erfahrungen habe. Hier kann ich relativ wenig im moment zu sagen. Vielleicht muss man ein paar Ideen testen und dann ausbessern, was einem nicht gefällt.  
  > Stimmt. Ein Iterativer ansatz funktioniert fast immer am besten.  
Die Gebiete könnten durch einfache Kreise dargestellt werden  
? Malen wir einfache Kreise oder verwenden wir Grafiken für die Gebiete?  
  > In dem Blog von Codingame über eigenen Multiplayer Spiele steht, dass man lieber auf Grafiken, statt auf Formen setzen soll (weshalb auch immer). Vielleicht kann man das von Ghost in the Cell etwas abgucken.  
    > Sieht wahrscheinlich einfach besser aus :D Vielleicht krieg ich auch mit GIMP schnell was zusammengebastelt. Wird bestimmt nicht zu viel Aufwand.  
Die Verbindungen der Gebiete kann man mit einfachen Linien machen  

An den Gebieten soll man erkennen können...  
  - ... wer das gebiet kontrolliert (Farbe des Spielers)
  - ... wie viele Einheiten in dem Gebiet sind (einfach eine Zahl dran schreiben)
  - ... Zu welcher Region ein einzelnes Gebiet gehört  
    ? Machen wir das mit Farben? Haben wir so viele Farben, die man gut unterscheiden kann?  
      > Hier bin ich mir nicht sicher was am besten ist. Vielleicht ein umris der alle Gebiete einschließt und die Region hat einen Buchstaben + eine Farbe? Die Farben können wir vorab alle wählen, weil wir die maximale Anzahl an Regionen begrenzen sollten.  
      > Vielleicht auch einfach einen Buchstaben, den man neben das Feld schreibt. Dann kann man die auf jeden fall unterscheiden. Und Buchstaben haben wir mehr als Farben, die man unterscheiden kann.  
Welche Region wie viele Einheiten bringt, sollte auch irgentwo dargestellt werden
  ? Besser an jeder Region, oder zusammen in einer art Legende?  
    > Ich hätte zuerst an eine art Legende gedacht, aber das hat beides Vor- und Nachteile.  
Es muss dargestellt werden, welcher Spieler ...  
  - ... wie viele Gebiete Kontrolliert
  - ... wie viele Einheiten zu Beginn der Runde erhällt
  - ... was sonst noch als Siegbedingung wichtig ist (als Tie-breaker, wenn die Spieler gleich viele Gebiete kontrollieren)
  
? Wenn wir einen Graphen generieren (als Karte): Wie berechnen wir die Positionen auf der Karte, sodass die Verbindungen sich möglichst wenig überlappen? Es gibt bestimmt schon einen Algorithmus dafür.  
  > Überlappungen sollten wir nicht zulassen und das müssen wir beim Map generator Berücksichtigen. Übergänge zwischen verschiedenen Regionen müssten notfalls über den Bildschirmrand möglich sein (und durch die linien dargestellt werden).  
Die Bewegungen der Einheiten kann man darstellen indem man die Zahl (oder einen Kreis mit einer Zahl) über die Linie bewegt.  
? Wie stellen wir am besten dar, wie viele Einheiten zerstört wurden?
  > Vielleicht ähnlich wie bei warzone.com, aber ich bin mir noch nicht sicher. Alternativ zwei Boxen/Felder an einer Line mit Verlusten für je eine Seite oder ein Feld bei der Linie und eines im Land. Hmm...  

### Sonstiges

? Hab ich noch was vergessen? ... vermutlich ja :P
> Echt coole ideen insgesamt! Die Sachen die ich nicht nochmal extra kommentiert habe sehe ich genauso/finde ich gut so.  
> Insgesamt errinert mich das Spiel ein wenig an Ghost in the Cell (heißt mittlerweile Cyborg Uprising). Ich finde die Unterschiede aber sehr interresant. Also es lohnt sich daraus ein extra Spiel zu machen.  
  > Stimmt, ich hatte anfangs auch ein wenig sorge, dass das zu ähnlich ist, aber es hat ein paar wichtige Unterschiede und bietet neue Möglichkeiten. Ich glaube auch, dass das sich lohnt :)  

? Was ist mit Spezialzügen? Brauchen wir sowas? Falls ja, welche?  
> Ich glaube wir haben schon genug Regeln, sodass wir nicht unbedingt noch Spezialzüge bräuchten. Aber wenn wir eine coole Idee dafür haben können wir die vielleicht noch einbauen.  
> Wir sollten aber darauf achten, nicht zu viele Regeln einzubauen. Sonst muss man zu viel an der KI anpassen um das zu Spielen. Das war auch einer meiner Hauptkritikpunkte bei "Green Circle". Genau, erstmal keine Sonderzüge, das Spiel hat so schon genug.  






