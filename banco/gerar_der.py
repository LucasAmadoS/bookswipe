# Gera o DER do BookSwipe (DER.svg). Uso: python gerar_der.py
# Depois de mudar o banco, atualize TABLES/relacionamentos aqui e rode de novo.
import math, os

W, H = 2300, 1360
TW, HEAD, ROW, PAD = 340, 46, 30, 10
COL = [60, 520, 980, 1440, 1900]
ROW_Y = {"A": 220, "B": 530, "C": 930}

GROUPS = {
    "pessoas":  ("#7c3aed", "Pessoas"),
    "catalogo": ("#2563eb", "Catálogo"),
    "social":   ("#db2777", "Rede social"),
    "troca":    ("#059669", "Troca sustentável"),
}

PK, FK, UK, PF = ["PK"], ["FK"], ["UK"], ["PK", "FK"]
TABLES = {
    "genero": (0, "B", "catalogo", [("id", "INT", PK), ("nome", "VARCHAR(50)", UK)]),
    "livro": (1, "B", "catalogo", [
        ("id", "INT", PK), ("titulo", "VARCHAR(200)", []), ("autor", "VARCHAR(150)", []),
        ("isbn", "VARCHAR(20)", []), ("sinopse", "TEXT", []), ("capa_url", "VARCHAR(500)", []),
        ("genero_id", "INT", FK)]),
    "curtida": (2, "A", "social", [
        ("id", "INT", PK), ("usuario_id", "INT", FK), ("experiencia_id", "INT", FK),
        ("tipo", "VARCHAR(10)", []), ("data_curtida", "DATETIME", [])]),
    "experiencia": (2, "B", "social", [
        ("id", "INT", PK), ("usuario_id", "INT", FK), ("livro_id", "INT", FK),
        ("nota", "INT (0 a 5)", []), ("sentimento", "VARCHAR(50)", []), ("texto", "TEXT", []),
        ("trecho_favorito", "VARCHAR(500)", []), ("data_post", "DATETIME", [])]),
    "comentario": (2, "C", "social", [
        ("id", "INT", PK), ("usuario_id", "INT", FK), ("experiencia_id", "INT", FK),
        ("texto", "VARCHAR(1000)", []), ("data_comentario", "DATETIME", [])]),
    "estante": (3, "A", "pessoas", [
        ("usuario_id", "INT", PF), ("livro_id", "INT", PF),
        ("status", "VARCHAR(20)", []), ("favorito", "BOOLEAN", [])]),
    "usuario": (3, "B", "pessoas", [
        ("id", "INT", PK), ("nome", "VARCHAR(100)", []), ("email", "VARCHAR(100)", UK),
        ("senha", "VARCHAR(100)", []), ("curso", "VARCHAR(100)", []), ("bairro", "VARCHAR(100)", []),
        ("data_cadastro", "DATETIME", [])]),
    "livro_disponivel": (3, "C", "troca", [
        ("id", "INT", PK), ("usuario_id", "INT", FK), ("livro_id", "INT", FK),
        ("condicao", "VARCHAR(20)", []), ("status", "VARCHAR(20)", [])]),
    "usuario_genero": (4, "B", "pessoas", [("usuario_id", "INT", PF), ("genero_id", "INT", PF)]),
    "sessao": (4, "A", "pessoas", [
        ("token_hash", "CHAR(64)", PK), ("usuario_id", "INT", FK),
        ("criada_em", "DATETIME", []), ("expira_em", "DATETIME", [])]),
    "solicitacao_troca": (4, "C", "troca", [
        ("id", "INT", PK), ("livro_disponivel_id", "INT", FK), ("solicitante_id", "INT", FK),
        ("status", "VARCHAR(20)", []), ("data_solicitacao", "DATETIME", []),
        ("data_conclusao", "DATETIME", [])]),
}

# Relacionamentos: (pontos do lado 1 até o lado N, rótulo, posição do rótulo)
RELS = [
    ([(400, 588), (520, 588)], "classifica", (460, 562)),
    ([(860, 650), (980, 650)], "é avaliado em", (920, 624)),
    ([(1150, 530), (1150, 426)], "recebe", (1150, 478)),
    ([(1150, 826), (1150, 930)], "recebe", (1150, 878)),
    ([(1440, 680), (1320, 680)], "escreve", (1380, 654)),
    ([(1440, 570), (1380, 570), (1380, 323), (1320, 323)], "dá", (1380, 446)),
    ([(1440, 760), (1380, 760), (1380, 1033), (1320, 1033)], "faz", (1380, 896)),
    ([(1610, 530), (1610, 396)], "organiza", (1610, 463)),
    ([(690, 530), (690, 170), (1560, 170), (1560, 220)], "está na estante", (1125, 170)),
    ([(1610, 796), (1610, 930)], "oferece", (1610, 863)),
    ([(690, 796), (690, 1210), (1560, 1210), (1560, 1136)], "é exemplar de", (1125, 1210)),
    ([(1780, 1060), (1900, 1060)], "recebe", (1840, 1034)),
    ([(1780, 760), (1840, 760), (1840, 990), (1900, 990)], "solicita", (1840, 875)),
    ([(1780, 588), (1900, 588)], "prefere", (1840, 562)),
    ([(230, 646), (230, 1265), (2270, 1265), (2270, 588), (2240, 588)], "é preferido por", (1300, 1265)),
    ([(1700, 530), (1700, 470), (2070, 470), (2070, 396)], "abre", (1885, 470)),
]

BADGE = {"PK": ("#d97706", "#fff7e6"), "FK": ("#2563eb", "#e8f0ff"), "UK": ("#64748b", "#eef2f6")}
DESC = {"PK": "chave primária", "FK": "chave estrangeira", "UK": "valor único"}

out = []
add = out.append

def table(name):
    c, r, g, cols = TABLES[name]
    x, y = COL[c], ROW_Y[r]
    color = GROUPS[g][0]
    h = HEAD + len(cols) * ROW + PAD
    add(f'<rect x="{x}" y="{y}" width="{TW}" height="{h}" rx="12" fill="#ffffff" stroke="#d6dbe3" filter="url(#sombra)"/>')
    add(f'<path d="M{x},{y+12} a12,12 0 0 1 12,-12 h{TW-24} a12,12 0 0 1 12,12 v{HEAD-12} h-{TW} z" fill="{color}"/>')
    add(f'<text x="{x+18}" y="{y+30}" class="th">{name}</text>')
    for i, (col, tipo, keys) in enumerate(cols):
        ry = y + HEAD + i * ROW
        if i % 2 == 1:
            add(f'<rect x="{x+1}" y="{ry}" width="{TW-2}" height="{ROW}" fill="#f7f8fb"/>')
        for j, k in enumerate(keys):
            fg, bg = BADGE[k]
            bx = x + 16 + j * 34
            add(f'<rect x="{bx}" y="{ry+7}" width="30" height="17" rx="4" fill="{bg}" stroke="{fg}"/>')
            add(f'<text x="{bx+15}" y="{ry+20}" class="bd" fill="{fg}" text-anchor="middle">{k}</text>')
        weight = ' font-weight="600"' if "PK" in keys else ""
        add(f'<text x="{x+90}" y="{ry+20}" class="col"{weight}>{col}</text>')
        add(f'<text x="{x+TW-16}" y="{ry+20}" class="ty" text-anchor="end">{tipo}</text>')

def one(px, py, dx, dy, cls="ln"):
    """Duas barras = exatamente 1."""
    nx, ny = -dy, dx
    s = ""
    for d in (12, 20):
        cx, cy = px + dx * d, py + dy * d
        s += f'<line x1="{cx+nx*9}" y1="{cy+ny*9}" x2="{cx-nx*9}" y2="{cy-ny*9}" class="{cls}"/>'
    return s

def many(px, py, dx, dy, cls="ln"):
    """Pé de galinha + círculo = 0 ou muitos."""
    nx, ny = -dy, dx
    bx, by = px + dx * 18, py + dy * 18
    s = "".join(f'<line x1="{bx}" y1="{by}" x2="{px+nx*10*k}" y2="{py+ny*10*k}" class="{cls}"/>' for k in (-1, 0, 1))
    return s + f'<circle cx="{px+dx*27}" cy="{py+dy*27}" r="6" fill="#fff" class="{cls}"/>'

def unit(a, b):
    L = math.hypot(b[0] - a[0], b[1] - a[1])
    return (b[0] - a[0]) / L, (b[1] - a[1]) / L

def label(x, y, txt):
    w = len(txt) * 7.3 + 20
    add(f'<rect x="{x-w/2}" y="{y-11}" width="{w}" height="22" rx="11" fill="#ffffff" stroke="#cbd2dc"/>')
    add(f'<text x="{x}" y="{y+4.5}" class="lb" text-anchor="middle">{txt}</text>')

# título + legenda de cores
add('<text x="60" y="62" class="t1">BookSwipe Sustentável</text>')
add('<text x="60" y="94" class="t2">Diagrama Entidade-Relacionamento · banco <tspan font-weight="700">bookswipe</tspan> (MySQL) · 11 tabelas</text>')
for i, (c, nome) in enumerate(GROUPS.values()):
    xx = 1500 + i * 190
    add(f'<rect x="{xx}" y="52" width="16" height="16" rx="4" fill="{c}"/>')
    add(f'<text x="{xx+24}" y="65" class="lg">{nome}</text>')

# linhas primeiro, tabelas por cima
for pts, txt, pos in RELS:
    add('<path d="M' + " L".join(f"{x},{y}" for x, y in pts) + '" class="ln" fill="none"/>')
    add(one(*pts[0], *unit(pts[0], pts[1])))
    add(many(*pts[-1], *unit(pts[-1], pts[-2])))
for t in TABLES:
    table(t)
for pts, txt, pos in RELS:
    label(*pos, txt)

# legenda da notação
ny = 1325
add(f'<text x="60" y="{ny}" class="lg" font-weight="600">Notação:</text>')
add(f'<line x1="150" y1="{ny-5}" x2="200" y2="{ny-5}" class="ln"/>' + one(150, ny - 5, 1, 0))
add(f'<text x="212" y="{ny}" class="lg">exatamente 1</text>')
add(f'<line x1="340" y1="{ny-5}" x2="390" y2="{ny-5}" class="ln"/>' + many(390, ny - 5, -1, 0))
add(f'<text x="402" y="{ny}" class="lg">0 ou muitos (1:N)</text>')
for i, k in enumerate(("PK", "FK", "UK")):
    fg, bg = BADGE[k]
    xx = 600 + i * 200
    add(f'<rect x="{xx}" y="{ny-14}" width="30" height="17" rx="4" fill="{bg}" stroke="{fg}"/>')
    add(f'<text x="{xx+15}" y="{ny-1}" class="bd" fill="{fg}" text-anchor="middle">{k}</text>')
    add(f'<text x="{xx+38}" y="{ny}" class="lg">{DESC[k]}</text>')
add(f'<text x="1220" y="{ny}" class="lg">PK + FK = chave composta (tabelas N:N)</text>')

svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="{W}" height="{H}" viewBox="0 0 {W} {H}">
<defs>
  <filter id="sombra" x="-10%" y="-10%" width="120%" height="130%">
    <feDropShadow dx="0" dy="4" stdDeviation="8" flood-color="#1e293b" flood-opacity="0.12"/>
  </filter>
  <style>
    text {{ font-family: "Segoe UI", Arial, sans-serif; }}
    .t1 {{ font-size: 36px; font-weight: 700; fill: #0f172a; }}
    .t2 {{ font-size: 18px; fill: #475569; }}
    .th {{ font-size: 18px; font-weight: 700; fill: #ffffff; }}
    .col {{ font-size: 15px; fill: #1e293b; }}
    .ty {{ font-size: 12.5px; fill: #64748b; font-family: Consolas, monospace; }}
    .bd {{ font-size: 10.5px; font-weight: 700; }}
    .lb {{ font-size: 13px; fill: #334155; font-style: italic; }}
    .lg {{ font-size: 15px; fill: #334155; }}
    .ln {{ stroke: #475569; stroke-width: 2; }}
  </style>
</defs>
<rect width="{W}" height="{H}" fill="#f1f4f9"/>
{chr(10).join(out)}
</svg>'''

aqui = os.path.dirname(os.path.abspath(__file__))
with open(os.path.join(aqui, "DER.svg"), "w", encoding="utf-8") as f:
    f.write(svg)
print("DER.svg gerado")
