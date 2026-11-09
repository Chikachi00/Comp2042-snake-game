"""Build the final high-level Design.pdf; requires Python and reportlab.

Run from any directory: python docs/build_design.py
Use --project-root and --output to render a review copy without changing the project.
Production Java declarations and selected retained-reference types are checked first.
"""
from __future__ import annotations

import argparse
from dataclasses import dataclass
from math import atan2, cos, sin
from pathlib import Path
import re

from reportlab.lib.colors import HexColor, white
from reportlab.lib.pagesizes import A3, landscape
from reportlab.pdfgen.canvas import Canvas
from reportlab.pdfbase.pdfmetrics import stringWidth

W, H = landscape(A3)
INK = HexColor('#000000')
MUTED = HexColor('#444444')
TOP_LEVEL = {
    'entry': ['Main', 'SnakeApplication'],
    'ui': ['ViewManager', 'MainMenuController', 'GameController', 'GameView', 'GameFlow'],
    'persistence': ['HighScoreStore'],
    'game': ['Game', 'GameSession', 'GameMode', 'GameSpeed', 'Level', 'Snake', 'Obstacle', 'Direction',
             'ComboTracker', 'Shortcut', 'ShortcutSnapshot', 'Coin', 'CoinType',
             'CoinFactory', 'CoinEffectService', 'GoldCoin', 'BlueCoin', 'RainbowCoin', 'SilverCoin'],
}
AREA = {name: area for area, names in TOP_LEVEL.items() for name in names}
AREA.update({'GameFlow.State': 'ui', 'GameSession.StepResult': 'game', 'Shortcut.State': 'game',
             'Application': 'external'})
KINDS = {name: 'class' for name in AREA}
for name in ['GameMode', 'GameSpeed', 'Level', 'Direction', 'CoinType', 'GameFlow.State',
             'GameSession.StepResult', 'Shortcut.State']:
    KINDS[name] = 'enumeration'
KINDS['ShortcutSnapshot'] = 'record'
KINDS['Application'] = 'external, abstract'


def namespace(name):
    area = AREA[name]
    return 'javafx.application' if area == 'external' else 'com.comp2042' + ('' if area == 'entry' else '.' + area)


@dataclass(frozen=True)
class Box:
    name: str
    x: float
    y: float
    w: float = 200
    h: float = 56

    def port(self, side, offset=0):
        return {'L': (self.x, self.y + self.h/2 + offset),
                'R': (self.x+self.w, self.y + self.h/2 + offset),
                'T': (self.x+self.w/2 + offset, self.y),
                'B': (self.x+self.w/2 + offset, self.y+self.h)}[side]


class Sheet:
    def __init__(self, title, subtitle, boxes):
        self.title, self.subtitle = title, subtitle
        self.boxes = {b.name: b for b in boxes}
        self.edges, self.notes = [], []

    def edge(self, source, target, points, kind='association', text='', label=None,
             cardinality='', cardinal_at=None):
        self.edges.append((source, target, points, kind, text, label, cardinality, cardinal_at))

    def direct(self, source, source_side, target, target_side, **kwargs):
        self.edge(source, target, [self.boxes[source].port(source_side),
                                  self.boxes[target].port(target_side)], **kwargs)


def sheets():
    p = Sheet('Application and UI',
              'Entry points, JavaFX presentation, navigation and the shared record store.', [
        Box('Main', 45, 120, 175), Box('SnakeApplication', 350, 120, 220),
        Box('Application', 850, 120, 280), Box('ViewManager', 350, 265, 220),
        Box('HighScoreStore', 850, 265, 280), Box('MainMenuController', 350, 410, 220),
        Box('GameMode', 45, 410), Box('GameSpeed', 45, 265), Box('Level', 45, 555),
        Box('GameController', 350, 555, 220), Box('GameView', 650, 555),
        Box('GameSession', 950, 555), Box('GameFlow', 350, 690, 220),
        Box('GameFlow.State', 650, 690),
    ])
    p.direct('Main', 'R', 'SnakeApplication', 'L', kind='dependency', text='launches', label=(285, 136))
    p.direct('SnakeApplication', 'R', 'Application', 'L', kind='inheritance')
    p.direct('SnakeApplication', 'B', 'ViewManager', 'T', kind='dependency', text='creates', label=(498, 224))
    p.edge('ViewManager', 'MainMenuController', [(415,321),(415,410)], text='mainMenuController', label=(317,369), cardinality='1', cardinal_at=(402,399))
    p.edge('MainMenuController', 'ViewManager', [(510,410),(510,321)], text='viewManager', label=(568,369), cardinality='1', cardinal_at=(524,338))
    p.direct('ViewManager', 'R', 'HighScoreStore', 'L', text='highScores', label=(710,281), cardinality='1', cardinal_at=(834,281))
    p.edge('GameController', 'HighScoreStore', [(350,595),(300,595),(300,347),(990,347),(990,321)], text='highScores (shared)', label=(762,337), cardinality='1', cardinal_at=(1004,337))
    p.direct('MainMenuController', 'L', 'GameMode', 'R', kind='dependency', text='selects mode', label=(298,427))
    p.edge('MainMenuController', 'GameSpeed', [(380,410),(380,360),(145,360),(145,321)],
           kind='dependency', text='selects speed', label=(242,350))
    p.edge('MainMenuController', 'Level', [(350,452),(280,452),(280,583),(245,583)], kind='dependency', text='selects start', label=(261,516))
    p.edge('HighScoreStore', 'GameMode', [(850,305),(800,305),(800,386),(145,386),(145,410)], kind='dependency', text='keys records by', label=(713,376))
    for target, x in [('GameController',460), ('GameView',750), ('GameSession',1050)]:
        p.edge('MainMenuController', target, [(460,466),(460,503),(x,503),(x,555)], kind='dependency',
               text='creates on Start' if target == 'GameSession' else '', label=(780,492))
    p.direct('GameController', 'R', 'GameView', 'L', text='gameView', label=(610,573), cardinality='1', cardinal_at=(635,598))
    p.edge('GameController', 'GameSession', [(570,600),(610,600),(610,646),(1050,646),(1050,611)], text='session', label=(805,636), cardinality='1', cardinal_at=(1064,628))
    p.direct('GameController', 'B', 'GameFlow', 'T', kind='composition', text='flow', label=(490,657), cardinality='1', cardinal_at=(474,680))
    p.direct('GameFlow', 'R', 'GameFlow.State', 'L', text='state', label=(610,707), cardinality='1', cardinal_at=(634,733))
    p.notes = []

    q = Sheet('Game state and shortcuts',
              'Current references and snapshot conversion. Both modes use the same session coordinator.', [
        Box('Game',45,120,180), Box('Snake',340,120,180), Box('Obstacle',635,120,180), Box('Coin',930,120,180),
        Box('GameSession',475,300,220), Box('ComboTracker',45,300,180), Box('Level',930,300,180),
        Box('GameSession.StepResult',45,460), Box('GameMode',930,460,180),
        Box('Shortcut',475,490,220), Box('GameView',930,550,180),
        Box('Direction',45,580), Box('GameController',45,690,220),
        Box('Shortcut.State',475,670,220), Box('ShortcutSnapshot',930,670,220),
    ])
    q.edge('GameSession','Game',[(505,300),(505,232),(135,232),(135,176)], text='game',label=(262,221),cardinality='1',cardinal_at=(149,191))
    q.edge('GameSession','Snake',[(555,300),(555,211),(430,211),(430,176)],text='snake',label=(491,200),cardinality='1',cardinal_at=(444,191))
    q.edge('GameSession','Obstacle',[(605,300),(605,232),(725,232),(725,176)],text='obstacle',label=(675,221),cardinality='1',cardinal_at=(739,191))
    q.edge('GameSession','Coin',[(655,300),(655,254),(1020,254),(1020,176)],text='coin',label=(854,243),cardinality='1',cardinal_at=(1034,191))
    q.edge('Snake','Obstacle',[(520,148),(635,148)],text='obstacle',label=(577,136),cardinality='1',cardinal_at=(620,163))
    q.edge('Snake','Direction',[(340,148),(280,148),(280,608),(245,608)],text='direction',label=(247,277),cardinality='1',cardinal_at=(260,624))
    q.direct('GameSession','L','ComboTracker','R',kind='composition',text='combo',label=(349,316),cardinality='1',cardinal_at=(241,342))
    q.edge('GameSession','Level',[(695,313),(930,313)],text='level',label=(812,302),cardinality='0..1',cardinal_at=(909,302))
    q.edge('GameSession','Level',[(695,344),(930,344)],text='startingLevel',label=(812,334),cardinality='0..1',cardinal_at=(909,359))
    q.edge('GameSession','GameMode',[(650,356),(650,412),(900,412),(900,488),(930,488)],kind='dependency',text='derives mode',label=(785,401))
    q.edge('GameSession','GameSession.StepResult',[(475,344),(320,344),(320,488),(245,488)],kind='dependency',text='returns',label=(369,478))
    q.edge('GameController','GameSession.StepResult',[(265,718),(310,718),(310,533),(145,533),(145,516)],kind='dependency',text='uses result',label=(353,566))
    q.edge('GameController','Direction',[(155,690),(155,650),(145,650),(145,636)],text='direction',label=(214,665),cardinality='1',cardinal_at=(169,646))
    q.edge('GameSession','Shortcut',[(585,356),(585,490)],kind='composition',text='shortcuts',label=(632,440),cardinality='0..2',cardinal_at=(608,478))
    q.edge('Level','Shortcut',[(1020,356),(1020,384),(725,384),(725,504),(695,504)],kind='dependency',text='creates fresh',label=(812,373))
    q.direct('Shortcut','B','Shortcut.State','T',text='state',label=(613,615),cardinality='1',cardinal_at=(599,659))
    q.edge('Shortcut','ShortcutSnapshot',[(695,518),(900,518),(900,698),(930,698)],kind='dependency',text='creates',label=(795,507))
    q.edge('GameView','ShortcutSnapshot',[(1020,606),(1020,640),(1040,640),(1040,670)],text='shortcuts',label=(1082,644),cardinality='0..2',cardinal_at=(1062,659))
    q.edge('Obstacle','ShortcutSnapshot',[(725,120),(725,100),(1170,100),(1170,716),(1150,716)],kind='dependency',text='reads snapshots',label=(1116,399))
    q.edge('GameView','GameSession',[(930,565),(825,565),(825,328),(695,328)],kind='dependency',text='renders',label=(759,318))
    q.edge('GameView','Game',[(930,591),(870,591),(870,108),(135,108),(135,120)],text='game',label=(847,282),cardinality='1',cardinal_at=(149,110))
    q.notes = [(45,370,195,[
        'Level references are optional in Endless.',
        'GameView caches Snake, Coin and',
        'Obstacle (0..1 each).',
    ])]

    r = Sheet('Coins and effects',
              'The concrete Coin class supplies the shared contract; four subclasses override category, value or effect.', [
        Box('GameSession',60,150,210), Box('CoinFactory',450,150,210), Box('CoinEffectService',850,150,240),
        Box('Coin',450,410,210), Box('CoinType',850,410,240),
        Box('GoldCoin',60,650), Box('BlueCoin',340,650), Box('RainbowCoin',620,650), Box('SilverCoin',900,650),
    ])
    r.direct('GameSession','R','CoinFactory','L',text='coinFactory',label=(360,166),cardinality='1',cardinal_at=(435,194))
    r.edge('GameSession','CoinEffectService',[(165,150),(165,113),(970,113),(970,150)],text='coinEffects',label=(707,109),cardinality='1',cardinal_at=(985,140))
    r.edge('GameSession','Coin',[(165,206),(165,438),(450,438)],text='coin',label=(300,427),cardinality='1',cardinal_at=(435,454))
    r.direct('CoinFactory','B','Coin','T',kind='dependency',text='creates concrete pickup',label=(644,309))
    r.edge('CoinEffectService','Coin',[(970,206),(970,320),(630,320),(630,410)],kind='dependency',text='invokes polymorphic effect',label=(829,309))
    r.direct('Coin','R','CoinType','L',kind='dependency',text='reports category',label=(755,427))
    r.edge('CoinFactory','CoinType',[(660,178),(750,178),(750,270),(1140,270),(1140,438),(1090,438)],kind='dependency',text='selects category',label=(964,259))
    for name, x in [('GoldCoin',160),('BlueCoin',440),('RainbowCoin',720),('SilverCoin',1000)]:
        r.edge(name,'Coin',[(x,650),(x,556),(555,556),(555,466)],kind='inheritance')
    r.notes = [(220,282,220,[
        'CoinFactory and CoinEffectService also',
        'use Game, Snake and Obstacle (page 2).',
    ])]
    return [p,q,r]


def draw_label(c, x, y, text, size=10):
    width = c.stringWidth(text, 'Helvetica', size)
    c.setFillColor(white)
    c.rect(x-width/2-3, H-y-3, width+6, size+5, fill=1, stroke=0)
    c.setFillColor(INK); c.setFont('Helvetica',size)
    c.drawCentredString(x,H-y,text)


def arrow(c, tip, previous, kind):
    x,y = tip; px,py = previous
    angle = atan2(y-py,x-px)
    length, wing = (12,.44) if kind == 'inheritance' else (8,.43)
    ends = [(x-length*cos(angle-a),y-length*sin(angle-a)) for a in [-wing,wing]]
    if kind == 'inheritance':
        path=c.beginPath();path.moveTo(x,H-y)
        for ex,ey in ends:path.lineTo(ex,H-ey)
        path.close();c.setFillColor(white);c.drawPath(path,fill=1,stroke=1)
    else:
        for ex,ey in ends:c.line(x,H-y,ex,H-ey)


def draw(c, sheet, number, total):
    c.setFillColor(INK);c.setFont('Helvetica',10)
    c.drawString(40,H-34,'COMP2042 Class diagram')
    c.setFont('Helvetica-Bold',15);c.drawString(40,H-63,sheet.title)
    for source,target,points,kind,text,at,mult,mult_at in sheet.edges:
        c.setLineWidth(1);c.setStrokeColor(INK);c.setDash(5,3) if kind=='dependency' else c.setDash()
        path=c.beginPath();path.moveTo(points[0][0],H-points[0][1])
        for x,y in points[1:]:path.lineTo(x,H-y)
        c.drawPath(path);c.setDash()
        previous=next(p for p in reversed(points[:-1]) if p!=points[-1])
        arrow(c,points[-1],previous,kind)
        if kind=='composition':
            x,y=points[0];px,py=next(p for p in points[1:] if p!=points[0]);a=atan2(py-y,px-x)
            path=c.beginPath();path.moveTo(x,H-y)
            for distance,offset in [(6,4),(12,0),(6,-4)]:
                path.lineTo(x+distance*cos(a)-offset*sin(a),H-(y+distance*sin(a)+offset*cos(a)))
            path.close();c.setFillColor(INK);c.drawPath(path,fill=1)
    for box in sheet.boxes.values():
        c.setFillColor(white);c.setStrokeColor(INK);c.setLineWidth(1)
        c.rect(box.x,H-box.y-box.h,box.w,box.h,fill=1,stroke=1)
        kind=KINDS[box.name]
        if kind in ('enumeration', 'record') or box.name == 'Application':
            kind = 'external' if box.name == 'Application' else kind
            c.setFillColor(MUTED);c.setFont('Helvetica',8.5)
            c.drawCentredString(box.x+box.w/2,H-box.y-12,'<<'+kind+'>>')
        c.setFillColor(INK);c.setFont('Helvetica-Oblique' if box.name=='Application' else 'Helvetica-Bold',13)
        c.drawCentredString(box.x+box.w/2,H-box.y-31,box.name)
    for source,target,points,kind,text,at,mult,mult_at in sheet.edges:
        if text:draw_label(c,*at,text)
        if mult:draw_label(c,*mult_at,mult)
    for x,y,width,lines in sheet.notes:
        c.setFont('Helvetica',9);c.setFillColor(MUTED)
        for i,line in enumerate(lines):c.drawString(x+9,H-y-16-i*14,line)
    c.setFillColor(MUTED);c.setFont('Helvetica',8.5)
    if number == 1:
        c.drawString(40,45,'Solid arrow: association | Dashed arrow: dependency | Filled diamond: composition | Open triangle: inheritance')
        c.drawString(40,30,'Multiplicities apply at the target end. Crossings are not junctions. Repeated class boxes represent the same type.')
    c.drawRightString(W-40,20,f'{number} / {total}')
    c.showPage()


def validate(project_root, pages):
    source_root=project_root/'src/main/java'
    expected={namespace(name)+'.'+name for name in AREA if name!='Application'}
    found=set()
    declaration=re.compile(r'^\s*(?:(?:public|private|protected|static|final|abstract)\s+)*(?:class|enum|record|interface)\s+(\w+)',re.M)
    for file in source_root.rglob('*.java'):
        text=file.read_text(encoding='utf-8')
        package=re.search(r'^package\s+([\w.]+);',text,re.M).group(1)
        for name in declaration.findall(text):
            found.add(package+'.'+(name if name==file.stem else file.stem+'.'+name))
    if found!=expected:
        raise ValueError(f'Source inventory mismatch: missing={found-expected}, obsolete={expected-found}')
    references={
        'ViewManager': [('HighScoreStore','highScores'),('MainMenuController','mainMenuController')],
        'MainMenuController': [('ViewManager','viewManager')],
        'GameController': [('GameSession','session'),('GameView','gameView'),('HighScoreStore','highScores'),
                           ('GameFlow','flow'),('Direction','direction')],
        'GameFlow': [('State','state')],
        'GameSession': [('Game','game'),('Snake','snake'),('Coin','coin'),('Obstacle','obstacle'),
                        ('CoinFactory','coinFactory'),('CoinEffectService','coinEffects'),('ComboTracker','combo'),
                        ('Level','level'),('Level','startingLevel'),('List<Shortcut>','shortcuts')],
        'GameView': [('Game','game'),('List<ShortcutSnapshot>','shortcuts'),
                     ('Snake','lastSnake'),('Coin','lastCoin'),('Obstacle','lastObstacle')],
        'Snake': [('Obstacle','obstacle'),('Direction','direction')],
        'Shortcut': [('State','state')],
        'HighScoreStore': [('EnumMap<GameMode,Integer>','scores')],
    }
    for owner, fields in references.items():
        file=source_root/Path(namespace(owner).replace('.','/'))/(owner+'.java')
        compact=re.sub(r'\s+','',file.read_text(encoding='utf-8'))
        for field_type, field_name in fields:
            if not re.search(re.escape(field_type+field_name)+r'(?=[=;])',compact):
                raise ValueError(f'Retained reference changed: {owner}.{field_name}: {field_type}')
    for child,parent in [('SnakeApplication','Application'),('GoldCoin','Coin'),('BlueCoin','Coin'),
                         ('RainbowCoin','Coin'),('SilverCoin','Coin')]:
        file=source_root/Path(namespace(child).replace('.','/'))/(child+'.java')
        if not re.search(r'\bclass\s+'+child+r'\s+extends\s+'+parent+r'\b',file.read_text(encoding='utf-8')):
            raise ValueError('Inheritance changed: '+child)
    visible={name for page in pages for name in page.boxes if name!='Application'}
    if visible != set(AREA)-{'Application'}:raise ValueError('A project classifier is missing from the views')
    for page in pages:
        for box in page.boxes.values():
            if not (35<=box.x<box.x+box.w<=W-15 and 100<=box.y<box.y+box.h<=755):
                raise ValueError('Classifier outside content region: '+box.name)
        boxes=list(page.boxes.values())
        for i,a in enumerate(boxes):
            for b in boxes[i+1:]:
                if a.x<b.x+b.w and b.x<a.x+a.w and a.y<b.y+b.h and b.y<a.y+a.h:
                    raise ValueError('Overlapping classifiers: '+a.name+', '+b.name)
        notes=[Box('note',x,y,width,len(lines)*14+16) for x,y,width,lines in page.notes]
        for x,y,width,lines in page.notes:
            for line in lines:
                if stringWidth(line,'Helvetica',9)>width-18:
                    raise ValueError('Note text exceeds its container: '+line)
        for note in notes:
            for box in boxes:
                if note.x<box.x+box.w and box.x<note.x+note.w and note.y<box.y+box.h and box.y<note.y+note.h:
                    raise ValueError('Note overlaps classifier: '+box.name)
        for source,target,points,*rest in page.edges:
            for a,b in zip(points,points[1:]):
                if a[0]!=b[0] and a[1]!=b[1]:raise ValueError(f'Non-orthogonal connector: {source}->{target}: {a}, {b}')
                for box in boxes+notes:
                    if box.name in (source,target):continue
                    if (a[0]==b[0] and box.x+1<a[0]<box.x+box.w-1 and max(min(a[1],b[1]),box.y+1)<min(max(a[1],b[1]),box.y+box.h-1)) or (a[1]==b[1] and box.y+1<a[1]<box.y+box.h-1 and max(min(a[0],b[0]),box.x+1)<min(max(a[0],b[0]),box.x+box.w-1)):
                        raise ValueError(f'Connector {source}->{target} crosses {box.name}')
    print('Verified 27 top-level project types, 3 nested enums, retained references and inheritance; diagram geometry is valid.')


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--project-root',type=Path,default=Path(__file__).resolve().parents[1])
    parser.add_argument('--output',type=Path)
    args=parser.parse_args();root=args.project_root.resolve();output=args.output or root/'Design.pdf'
    pages=sheets();validate(root,pages)
    output.parent.mkdir(parents=True,exist_ok=True)
    canvas=Canvas(str(output),pagesize=(W,H),pageCompression=1,invariant=1)
    canvas.setTitle('COMP2042 Snake Game - Final Class Diagram')
    canvas.setAuthor('COMP2042 coursework project')
    canvas.setSubject('Application, gameplay, shortcuts, personal records and coin inheritance')
    for i,page in enumerate(pages,1):draw(canvas,page,i,len(pages))
    canvas.save();print(output)


if __name__=='__main__':main()
