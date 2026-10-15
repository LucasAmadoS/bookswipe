// =============================================================
// api.js - tudo que todas as telas usam:
//   - chamar a API Java (fetch)
//   - lembrar quem está logado (localStorage)
//   - pequenos ajudantes de tela (capa, estrelas, aviso)
// =============================================================

const API = 'http://localhost:7070/api';

// Chama a API e devolve o JSON. Se a API responder erro, lança um Error com a mensagem dela.
// Ex: const livros = await api('/livros?busca=hobbit');
//     await api('/login', 'POST', { email, senha });
async function api(caminho, metodo = 'GET', corpo = null) {
  const opcoes = { method: metodo, headers: {} };
  if (corpo !== null) {
    opcoes.headers['Content-Type'] = 'application/json';
    opcoes.body = JSON.stringify(corpo); // objeto JS -> texto JSON
  }
  // O "crachá": toda chamada leva o token de sessão. É assim que a API sabe QUEM está pedindo.
  const token = sessao.token();
  if (token) {
    opcoes.headers['Authorization'] = 'Bearer ' + token;
  }

  let resposta;
  try {
    resposta = await fetch(API + caminho, opcoes);
  } catch (e) {
    throw new Error('Não consegui falar com a API. O ApiServidor está rodando?');
  }

  if (resposta.status === 204) {
    return null; // deu certo, sem conteúdo
  }
  // 401 = token vencido ou inválido: limpa a sessão e volta para o login
  if (resposta.status === 401 && !location.pathname.endsWith('login.html')) {
    localStorage.removeItem('bookswipe_sessao');
    location.href = 'login.html';
    throw new Error('Sua sessão expirou. Faça login de novo.');
  }
  const dados = await resposta.json(); // texto JSON -> objeto JS
  if (!resposta.ok) {
    throw new Error(dados.erro || 'Erro ' + resposta.status);
  }
  return dados;
}

// ---------- Sessão: quem está logado ----------
// O login devolve { token, usuario }. Os dois ficam guardados no navegador (localStorage),
// assim a pessoa continua logada ao trocar de página.
// O token é o que vale: o usuário guardado serve só para mostrar nome e iniciais na tela.
const sessao = {
  salvar(resposta) {
    localStorage.setItem('bookswipe_sessao', JSON.stringify({ token: resposta.token, usuario: resposta.usuario }));
  },
  dados() {
    try {
      return JSON.parse(localStorage.getItem('bookswipe_sessao')) || null;
    } catch (e) {
      return null;
    }
  },
  token() {
    return this.dados()?.token || null;
  },
  usuario() {
    const dados = this.dados();
    return dados && dados.token ? dados.usuario : null;
  },
  async sair() {
    try {
      await api('/logout', 'POST'); // apaga a sessão no banco: o token deixa de valer
    } catch (e) {
      // mesmo se a API estiver fora, sai do navegador
    }
    localStorage.removeItem('bookswipe_sessao');
    localStorage.removeItem('bookswipe_usuario'); // formato antigo, de antes do token
    location.href = 'index.html';
  },
};

// Páginas internas chamam isto: quem não fez login volta para a tela de login
function exigirLogin() {
  const usuario = sessao.usuario();
  if (!usuario) {
    location.href = 'login.html';
    throw new Error('Precisa fazer login');
  }
  return usuario;
}

// ---------- Segurança: escapar texto ----------
// Todo texto que veio de usuário passa por aqui antes de ir para o HTML.
// Sem isso, alguém poderia escrever <script> num comentário e rodar código na tela dos outros (XSS).
function esc(texto) {
  if (texto === null || texto === undefined) return '';
  return String(texto)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

// ---------- Ajudantes de tela ----------

// Lê ?id=3 da URL
function parametro(nome) {
  return new URLSearchParams(location.search).get(nome);
}

// 4 -> ★★★★☆
function estrelas(nota) {
  const n = Math.round(nota);
  return '<span class="estrelas">' + '★'.repeat(n) + '<span class="vazia">' + '★'.repeat(5 - n) + '</span></span>';
}

// Capa do livro: a imagem da Google Books se tiver; senão, uma capa desenhada com a cor do título
const CORES_CAPA = ['#1B3A2F', '#7A2E2E', '#2B4C7E', '#5B3A6B', '#8A5A1E', '#2E6B5E', '#3A3A3A', '#7E4B2B'];

function capa(livro, tamanho = '') {
  if (livro.capaUrl) {
    // Se a imagem não carregar (sem internet, link quebrado), troca pela capa desenhada
    return `<img class="capa ${tamanho}" src="${esc(livro.capaUrl)}" alt="Capa de ${esc(livro.titulo)}" loading="lazy"
                 data-titulo="${esc(livro.titulo)}" data-autor="${esc(livro.autor || '')}" data-tamanho="${tamanho}"
                 onerror="capaFalhou(this)">`;
  }
  let soma = 0;
  for (const letra of livro.titulo) soma += letra.charCodeAt(0);
  const cor = CORES_CAPA[soma % CORES_CAPA.length];
  return `<div class="capa capa-desenhada ${tamanho}" style="--cor-capa:${cor}" role="img" aria-label="Capa de ${esc(livro.titulo)}">
            <span class="capa-titulo">${esc(livro.titulo)}</span>
            <span class="capa-autor">${esc(livro.autor || '')}</span>
          </div>`;
}

function capaFalhou(img) {
  const caixa = document.createElement('div');
  caixa.innerHTML = capa({ titulo: img.dataset.titulo, autor: img.dataset.autor }, img.dataset.tamanho);
  img.replaceWith(caixa.firstElementChild);
}

// Iniciais para o avatar: "Ana Souza" -> "AS"
function iniciais(nome) {
  return nome.split(' ').filter(Boolean).slice(0, 2).map(p => p[0].toUpperCase()).join('');
}

// Avisinho que aparece no canto e some sozinho
function aviso(mensagem, tipo = 'ok') {
  const el = document.createElement('div');
  el.className = 'toast ' + tipo;
  el.textContent = mensagem;
  document.body.appendChild(el);
  setTimeout(() => el.classList.add('visivel'), 10);
  setTimeout(() => {
    el.classList.remove('visivel');
    setTimeout(() => el.remove(), 300);
  }, 3200);
}

// Mostra "carregando" e, se der erro, mostra a mensagem no lugar
async function carregar(elemento, funcao) {
  elemento.innerHTML = '<div class="carregando">Carregando…</div>';
  try {
    await funcao();
  } catch (e) {
    elemento.innerHTML = `<div class="vazio">⚠️ ${esc(e.message)}</div>`;
  }
}

// "2026-06-01T20:10:00" -> "01/06/2026"
function data(textoIso) {
  if (!textoIso) return '';
  const [ano, mes, dia] = textoIso.slice(0, 10).split('-');
  return `${dia}/${mes}/${ano}`;
}
