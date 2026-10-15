// =============================================================
// layout.js - monta o "esqueleto" das páginas internas:
// menu lateral, barra de cima (busca + usuário) e menu de baixo no celular.
// Cada página chama montarLayout('nome-da-pagina') para marcar o item ativo.
// =============================================================

const LOGO_SVG = `
  <svg class="logo-icone" viewBox="0 0 40 32" aria-hidden="true">
    <path d="M20 6C14 2 7 2 2 4v24c5-2 12-2 18 2 6-4 13-4 18-2V4c-5-2-12-2-18 2z"
          fill="none" stroke="currentColor" stroke-width="2.5" stroke-linejoin="round"/>
    <path d="M20 6v24" stroke="currentColor" stroke-width="2.5"/>
    <path d="M24 21c0-7 5-10 10-10 0 6-4 10-10 10z" fill="#4CAF7D"/>
  </svg>`;

const MENU = [
  { id: 'inicio',    icone: '🏠', nome: 'Início',         link: 'inicio.html' },
  { id: 'descobrir', icone: '🔥', nome: 'Descobrir',      link: 'descobrir.html' },
  { id: 'estante',   icone: '📚', nome: 'Minha estante',  link: 'estante.html' },
  { id: 'matches',   icone: '❤️', nome: 'Matches',        link: 'matches.html' },
  { id: 'trocas',    icone: '♻️', nome: 'Trocar livros',  link: 'trocas.html' },
  { id: 'impacto',   icone: '📊', nome: 'Meu impacto',    link: 'impacto.html' },
  { id: 'perfil',    icone: '👤', nome: 'Perfil',         link: 'perfil.html' },
];

// No celular só cabem 5 ícones embaixo
const MENU_CELULAR = ['inicio', 'descobrir', 'trocas', 'matches', 'perfil'];

function montarLayout(paginaAtiva) {
  const usuario = exigirLogin();
  const conteudo = document.getElementById('conteudo');

  // ----- Menu lateral -----
  const lateral = document.createElement('aside');
  lateral.className = 'lateral';
  lateral.innerHTML = `
    <a href="inicio.html" class="logo">${LOGO_SVG}<span>BookSwipe</span></a>
    <nav>
      ${MENU.map(item => `
        <a href="${item.link}" class="menu-item ${item.id === paginaAtiva ? 'ativo' : ''}">
          <span class="menu-icone">${item.icone}</span>${item.nome}
        </a>`).join('')}
    </nav>
    <a href="publicar.html" class="botao botao-destaque botao-publicar">✍️ Publicar</a>
    <p class="lema">Livros circulam,<br>ideias também.</p>`;

  // ----- Barra de cima -----
  const topo = document.createElement('header');
  topo.className = 'topo';
  topo.innerHTML = `
    <a href="inicio.html" class="logo logo-celular">${LOGO_SVG}<span>BookSwipe</span></a>
    <div class="busca">
      <input type="search" id="busca-topo" placeholder="🔍 Pesquisar livro ou autor..." autocomplete="off" aria-label="Pesquisar livro">
      <div class="busca-resultados" id="busca-resultados"></div>
    </div>
    <div class="usuario-topo">
      <a href="perfil.html" class="avatar" title="${esc(usuario.nome)}">${esc(iniciais(usuario.nome))}</a>
      <button class="botao-link" id="botao-sair">Sair</button>
    </div>`;

  // ----- Menu de baixo (celular) -----
  const menuCelular = document.createElement('nav');
  menuCelular.className = 'menu-celular';
  menuCelular.innerHTML = MENU.filter(i => MENU_CELULAR.includes(i.id)).map(item => `
    <a href="${item.link}" class="${item.id === paginaAtiva ? 'ativo' : ''}">
      <span>${item.icone}</span><small>${item.nome.split(' ')[0]}</small>
    </a>`).join('');

  // Monta: [lateral] [coluna: topo + conteúdo] + menu do celular
  const coluna = document.createElement('div');
  coluna.className = 'coluna';
  conteudo.parentNode.insertBefore(lateral, conteudo);
  conteudo.parentNode.insertBefore(coluna, conteudo);
  coluna.appendChild(topo);
  coluna.appendChild(conteudo);
  document.body.appendChild(menuCelular);

  document.getElementById('botao-sair').addEventListener('click', () => sessao.sair());
  ativarBusca();
  return usuario;
}

// Busca da barra de cima: mostra resultados enquanto digita
function ativarBusca() {
  const campo = document.getElementById('busca-topo');
  const caixa = document.getElementById('busca-resultados');
  let espera;

  campo.addEventListener('input', () => {
    clearTimeout(espera); // espera a pessoa parar de digitar (não chama a API a cada letra)
    const texto = campo.value.trim();
    if (texto.length < 2) {
      caixa.classList.remove('aberta');
      return;
    }
    espera = setTimeout(async () => {
      try {
        const livros = await api('/livros?busca=' + encodeURIComponent(texto));
        caixa.innerHTML = livros.length
          ? livros.slice(0, 6).map(l => `
              <a href="livro.html?id=${l.id}" class="busca-item">
                ${capa(l, 'mini')}
                <span><strong>${esc(l.titulo)}</strong><br><small>${esc(l.autor)}</small></span>
              </a>`).join('')
          : '<div class="busca-item vazio-mini">Nenhum livro encontrado</div>';
        caixa.classList.add('aberta');
      } catch (e) {
        caixa.innerHTML = `<div class="busca-item vazio-mini">${esc(e.message)}</div>`;
        caixa.classList.add('aberta');
      }
    }, 300);
  });

  document.addEventListener('click', e => {
    if (!e.target.closest('.busca')) caixa.classList.remove('aberta');
  });
}
