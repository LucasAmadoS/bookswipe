// Marketplace de trocas: disponíveis, pedidos recebidos, meus pedidos e oferecer livro

const usuario = montarLayout('trocas');
const STATUS_TEXTO = {
  PENDENTE: '🟡 Aguardando resposta', ACEITA: '🟢 Aceito', RECUSADA: '🔴 Recusado', CONCLUIDA: '♻️ Concluído',
};
let generoFiltro = 0;
let disponiveis = [];

// ---------- Abas ----------
document.querySelectorAll('.aba').forEach(aba => aba.addEventListener('click', () => abrirAba(aba.dataset.aba)));

function abrirAba(nome) {
  document.querySelectorAll('.aba').forEach(a => a.classList.toggle('ativa', a.dataset.aba === nome));
  ['disponiveis', 'recebidos', 'feitos', 'oferecer'].forEach(id => {
    document.getElementById('aba-' + id).hidden = id !== nome;
  });
  if (nome === 'disponiveis') carregarDisponiveis();
  if (nome === 'recebidos') carregarRecebidos();
  if (nome === 'feitos') carregarFeitos();
  if (nome === 'oferecer') carregarMeusOferecidos();
}

iniciar();

async function iniciar() {
  const generos = await api('/generos').catch(() => []);
  const caixa = document.getElementById('filtro-generos');
  caixa.innerHTML = [{ id: 0, nome: 'Todos' }, ...generos]
    .map(g => `<button class="chip-botao ${g.id === 0 ? 'ativo' : ''}" data-genero="${g.id}">${esc(g.nome)}</button>`).join('');
  caixa.querySelectorAll('button').forEach(botao => botao.addEventListener('click', () => {
    caixa.querySelectorAll('button').forEach(b => b.classList.toggle('ativo', b === botao));
    generoFiltro = Number(botao.dataset.genero);
    carregarDisponiveis();
  }));
  document.getElementById('filtro-texto').addEventListener('input', desenharDisponiveis);

  carregarDisponiveis();
  atualizarContadorRecebidos();
}

// ---------- Disponíveis ----------
async function carregarDisponiveis() {
  const caixa = document.getElementById('disponiveis');
  await carregar(caixa, async () => {
    disponiveis = await api(`/trocas/disponiveis?generoId=${generoFiltro}`);
    desenharDisponiveis();
  });
}

function desenharDisponiveis() {
  const caixa = document.getElementById('disponiveis');
  const texto = document.getElementById('filtro-texto').value.trim().toLowerCase();
  const lista = disponiveis.filter(ld => ld.livro.titulo.toLowerCase().includes(texto));

  if (lista.length === 0) {
    caixa.innerHTML = '<div class="card vazio">Nenhum livro disponível com esse filtro.</div>';
    return;
  }
  caixa.innerHTML = `<div class="grade">${lista.map(ld => `
    <div class="livro-card">
      ${capa(ld.livro, 'cheia')}
      <h3>${esc(ld.livro.titulo)}</h3>
      <p class="suave pequeno">${esc(ld.livro.autor)}</p>
      <p><span class="condicao ${ld.condicao}">● ${condicaoTexto(ld.condicao)}</span></p>
      <p class="local">📍 ${esc(ld.dono.bairro || 'Sem bairro')} · ${esc(ld.dono.nome.split(' ')[0])}</p>
      <button class="botao botao-pequeno" data-detalhes="${ld.id}">Ver livro</button>
    </div>`).join('')}</div>`;

  caixa.querySelectorAll('[data-detalhes]').forEach(botao => botao.addEventListener('click', () => {
    detalhes(disponiveis.find(ld => ld.id === Number(botao.dataset.detalhes)));
  }));
}

// Janela "Detalhes da troca" -> "Solicitação enviada!"
function detalhes(ld) {
  const fundo = document.createElement('div');
  fundo.className = 'modal-fundo';
  fundo.innerHTML = `
    <div class="modal">
      <div style="display:flex;gap:18px;margin-bottom:18px">
        ${capa(ld.livro, 'media')}
        <div>
          <h2>${esc(ld.livro.titulo)}</h2>
          <p class="suave pequeno" style="margin-bottom:14px">${esc(ld.livro.autor)}</p>
          <p class="pequeno">👤 Proprietário: <strong>${esc(ld.dono.nome)}</strong></p>
          <p class="pequeno">📗 Condição: <span class="condicao ${ld.condicao}">${condicaoTexto(ld.condicao)}</span></p>
          <p class="pequeno">📍 Localização: ${esc(ld.dono.bairro || '—')}</p>
        </div>
      </div>
      <p style="margin-bottom:16px">Você deseja solicitar este livro?</p>
      <div style="display:flex;gap:10px">
        <button class="botao botao-contorno" id="cancelar" style="flex:1">Cancelar</button>
        <button class="botao" id="solicitar" style="flex:2">Solicitar troca</button>
      </div>
    </div>`;
  document.body.appendChild(fundo);

  const fechar = () => fundo.remove();
  fundo.querySelector('#cancelar').addEventListener('click', fechar);
  fundo.addEventListener('click', e => { if (e.target === fundo) fechar(); });

  fundo.querySelector('#solicitar').addEventListener('click', async () => {
    try {
      await api('/trocas/pedidos', 'POST', { livroDisponivelId: ld.id });
      fundo.querySelector('.modal').innerHTML = `
        <div class="sucesso">
          <div class="check">✓</div>
          <h2>Solicitação enviada!</h2>
          <p class="suave" style="margin:8px 0 18px">Agora aguarde ${esc(ld.dono.nome.split(' ')[0])} aceitar sua solicitação.</p>
          <p><span class="status PENDENTE">${STATUS_TEXTO.PENDENTE}</span></p>
          <button class="botao botao-largo" style="margin-top:22px" id="ok">Entendi</button>
        </div>`;
      fundo.querySelector('#ok').addEventListener('click', fechar);
    } catch (e) {
      aviso(e.message, 'erro'); // ex: "Você já tem um pedido aberto para este livro"
    }
  });
}

// ---------- Pedidos recebidos (eu sou o dono) ----------
async function atualizarContadorRecebidos() {
  const pedidos = await api(`/usuarios/${usuario.id}/pedidos-recebidos`).catch(() => []);
  document.getElementById('contador-recebidos').textContent = pedidos.filter(p => p.status === 'PENDENTE').length;
}

async function carregarRecebidos() {
  const caixa = document.getElementById('recebidos');
  await carregar(caixa, async () => {
    const pedidos = await api(`/usuarios/${usuario.id}/pedidos-recebidos`);
    if (pedidos.length === 0) {
      caixa.innerHTML = '<p class="vazio">Ninguém pediu seus livros ainda.</p>';
      return;
    }
    caixa.innerHTML = pedidos.map(p => `
      <div class="pedido">
        ${capa(p.livroDisponivel.livro, 'mini')}
        <div class="pedido-info">
          <strong>${esc(p.solicitante.nome)}</strong> quer <strong>${esc(p.livroDisponivel.livro.titulo)}</strong>
          <div class="suave pequeno">Pedido em ${data(p.dataSolicitacao)}</div>
        </div>
        <span class="status ${p.status}">${STATUS_TEXTO[p.status]}</span>
        <div class="pedido-acoes">
          ${p.status === 'PENDENTE' ? `
            <button class="botao botao-pequeno" data-acao="aceitar" data-id="${p.id}">Aceitar</button>
            <button class="botao botao-perigo botao-pequeno" data-acao="recusar" data-id="${p.id}">Recusar</button>` : ''}
          ${p.status === 'ACEITA' ? `
            <button class="botao botao-escuro botao-pequeno" data-acao="concluir" data-id="${p.id}">♻️ Já entreguei</button>` : ''}
        </div>
      </div>`).join('');

    caixa.querySelectorAll('[data-acao]').forEach(botao => botao.addEventListener('click', async () => {
      try {
        await api(`/trocas/pedidos/${botao.dataset.id}/${botao.dataset.acao}`, 'PUT');
        const mensagens = {
          aceitar: 'Pedido aceito! Combine a entrega e depois marque "Já entreguei".',
          recusar: 'Pedido recusado.',
          concluir: 'Troca concluída! Mais um livro voltou a circular ♻️',
        };
        aviso(mensagens[botao.dataset.acao]);
        carregarRecebidos();
        atualizarContadorRecebidos();
      } catch (e) {
        aviso(e.message, 'erro');
      }
    }));
  });
}

// ---------- Meus pedidos ----------
async function carregarFeitos() {
  const caixa = document.getElementById('feitos');
  await carregar(caixa, async () => {
    const pedidos = await api(`/usuarios/${usuario.id}/pedidos-feitos`);
    caixa.innerHTML = pedidos.length
      ? pedidos.map(p => `
          <div class="pedido">
            ${capa(p.livroDisponivel.livro, 'mini')}
            <div class="pedido-info">
              <strong>${esc(p.livroDisponivel.livro.titulo)}</strong>
              <div class="suave pequeno">de ${esc(p.livroDisponivel.dono.nome)} · 📍 ${esc(p.livroDisponivel.dono.bairro || '—')} · ${data(p.dataSolicitacao)}</div>
            </div>
            <span class="status ${p.status}">${STATUS_TEXTO[p.status]}</span>
          </div>`).join('')
      : '<p class="vazio">Você ainda não pediu nenhum livro.</p>';
  });
}

// ---------- Oferecer ----------
let livroOferta = null;
const buscaOferta = document.getElementById('busca-oferta');
const opcoesOferta = document.getElementById('opcoes-oferta');
let esperaOferta;

buscaOferta.addEventListener('input', () => {
  clearTimeout(esperaOferta);
  esperaOferta = setTimeout(async () => {
    const texto = buscaOferta.value.trim();
    if (texto.length < 2) { opcoesOferta.innerHTML = ''; return; }
    const livros = await api('/livros?busca=' + encodeURIComponent(texto));
    opcoesOferta.innerHTML = livros.map(l =>
      `<div class="busca-item" data-id="${l.id}" style="cursor:pointer">${capa(l, 'mini')}<span>${esc(l.titulo)}<br><small class="suave">${esc(l.autor)}</small></span></div>`).join('')
      || '<p class="suave pequeno">Nenhum livro encontrado.</p>';
    opcoesOferta.querySelectorAll('[data-id]').forEach(item => item.addEventListener('click', () => {
      livroOferta = livros.find(l => l.id === Number(item.dataset.id));
      document.getElementById('oferta-escolhida').innerHTML = `Livro: <strong>${esc(livroOferta.titulo)}</strong>`;
      opcoesOferta.innerHTML = '';
      buscaOferta.value = '';
    }));
  }, 300);
});

document.getElementById('buscar-internet-oferta').addEventListener('click', () => {
  abrirBuscaNaInternet(buscaOferta.value.trim(), livro => {
    livroOferta = livro;
    document.getElementById('oferta-escolhida').innerHTML = `Livro: <strong>${esc(livro.titulo)}</strong>`;
  });
});

document.getElementById('form-oferecer').addEventListener('submit', async evento => {
  evento.preventDefault();
  const erro = document.getElementById('erro-oferta');
  erro.textContent = '';
  if (!livroOferta) { erro.textContent = 'Escolha o livro.'; return; }
  try {
    await api('/trocas/disponiveis', 'POST', {
      livroId: livroOferta.id, condicao: document.getElementById('condicao').value,
    });
    aviso(`${livroOferta.titulo} está disponível para troca! ♻️`);
    livroOferta = null;
    document.getElementById('oferta-escolhida').textContent = 'Nenhum livro escolhido.';
    carregarMeusOferecidos();
  } catch (e) {
    erro.textContent = e.message;
  }
});

async function carregarMeusOferecidos() {
  const caixa = document.getElementById('meus-oferecidos');
  await carregar(caixa, async () => {
    const lista = await api(`/usuarios/${usuario.id}/livros-oferecidos`);
    caixa.innerHTML = lista.length
      ? lista.map(ld => `
          <div class="pedido">
            ${capa(ld.livro, 'mini')}
            <div class="pedido-info"><strong>${esc(ld.livro.titulo)}</strong><div class="suave pequeno">${condicaoTexto(ld.condicao)}</div></div>
            <span class="status ${ld.status}">${ld.status}</span>
          </div>`).join('')
      : '<p class="suave">Você ainda não ofereceu nenhum livro.</p>';
  });
}

function condicaoTexto(condicao) {
  return { NOVO: 'Novo', BOA: 'Boa', REGULAR: 'Regular' }[condicao] || condicao;
}
