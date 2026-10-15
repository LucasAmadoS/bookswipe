// Descobrir: a pilha de cards do swipe.
// ❤ (direita) = GOSTEI, ✕ (esquerda) = PASSAR. Cada voto vai para a API e o card sai da fila.

const usuario = montarLayout('descobrir');
const pilha = document.getElementById('pilha');
const LIMITE_ARRASTE = 110; // quantos pixels arrastar para contar como voto

let fila = [];
let posicao = 0;
let ocupado = false; // evita votar duas vezes enquanto o card está saindo

iniciar();

async function iniciar() {
  try {
    fila = await api(`/usuarios/${usuario.id}/descobrir`);
    desenhar();
  } catch (e) {
    pilha.innerHTML = `<div class="vazio">⚠️ ${esc(e.message)}</div>`;
  }
}

function desenhar() {
  const atual = fila[posicao];
  const proximo = fila[posicao + 1];
  document.getElementById('contagem').textContent = atual
    ? `${posicao + 1} de ${fila.length} · arraste o card ou use as setas ← →`
    : '';

  if (!atual) {
    document.getElementById('controles').hidden = true;
    pilha.style.height = 'auto';
    pilha.innerHTML = `
      <div class="card vazio-grande">
        <div class="emoji">✨</div>
        <h2>Você viu tudo por aqui!</h2>
        <p class="suave" style="margin:8px 0 20px">Que tal contar o que achou de um livro?</p>
        <a href="publicar.html" class="botao botao-destaque">✍️ Publicar experiência</a>
      </div>`;
    return;
  }

  // O próximo fica atrás (menor e apagado) e o atual por cima
  pilha.innerHTML = (proximo ? cartao(proximo, 'atras') : '') + cartao(atual, '');
  ativarArraste(pilha.lastElementChild);
}

function cartao(exp, classe) {
  return `
    <article class="cartao-swipe ${classe}">
      <span class="selo selo-gostei">GOSTEI</span>
      <span class="selo selo-passar">PASSAR</span>
      <div class="cartao-topo">${capa(exp.livro, 'media')}</div>
      <div class="cartao-corpo">
        <h2>${esc(exp.livro.titulo)}</h2>
        <p class="suave pequeno">${esc(exp.livro.autor)}</p>
        <p>${estrelas(exp.nota)} ${exp.sentimento ? `<span class="chip chip-terracota">${esc(exp.sentimento)}</span>` : ''}</p>
        <p class="texto-exp">“${esc(exp.texto)}”</p>
        ${exp.trechoFavorito ? `<p class="trecho pequeno">“${esc(exp.trechoFavorito)}”</p>` : ''}
        <div class="cartao-autor">
          <span class="avatar">${esc(iniciais(exp.usuario.nome))}</span>
          <span>${esc(exp.usuario.nome)}</span>
          <span class="suave" style="margin-left:auto">❤️ ${exp.totalGostei} · 💬 ${exp.totalComentarios}</span>
        </div>
      </div>
    </article>`;
}

// ---------- Votar ----------
async function votar(tipo) {
  const exp = fila[posicao];
  if (!exp || ocupado) return;
  ocupado = true;
  const card = pilha.lastElementChild;

  try {
    await api(`/experiencias/${exp.id}/avaliacao`, 'POST', { tipo });
  } catch (e) {
    aviso(e.message, 'erro');
    card.style.transform = '';
    ocupado = false;
    return;
  }

  card.classList.remove('arrastando');
  card.style.transform = '';
  card.classList.add(tipo === 'GOSTEI' ? 'saiu-direita' : 'saiu-esquerda');
  if (tipo === 'GOSTEI') coracaoGrande();

  setTimeout(() => {   // espera a animação de saída terminar
    posicao++;
    ocupado = false;
    desenhar();
  }, 350);
}

function coracaoGrande() {
  const el = document.createElement('div');
  el.className = 'coracao-grande';
  el.textContent = '❤️';
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 700);
}

document.getElementById('botao-gostei').addEventListener('click', () => votar('GOSTEI'));
document.getElementById('botao-passar').addEventListener('click', () => votar('PASSAR'));
document.getElementById('botao-comentar').addEventListener('click', () => abrirComentarios(fila[posicao]));
document.addEventListener('keydown', e => {
  if (document.querySelector('.modal-fundo')) return; // não vota enquanto o modal está aberto
  if (e.key === 'ArrowRight') votar('GOSTEI');
  if (e.key === 'ArrowLeft') votar('PASSAR');
});

// ---------- Arrastar com mouse ou dedo ----------
function ativarArraste(card) {
  let inicioX = null;
  let deslocamento = 0;
  const seloGostei = card.querySelector('.selo-gostei');
  const seloPassar = card.querySelector('.selo-passar');

  card.addEventListener('pointerdown', e => {
    if (ocupado) return;
    inicioX = e.clientX;
    card.classList.add('arrastando');
    card.setPointerCapture(e.pointerId);
  });

  card.addEventListener('pointermove', e => {
    if (inicioX === null) return;
    deslocamento = e.clientX - inicioX;
    card.style.transform = `translateX(${deslocamento}px) rotate(${deslocamento / 18}deg)`;
    seloGostei.style.opacity = Math.max(0, deslocamento / LIMITE_ARRASTE);
    seloPassar.style.opacity = Math.max(0, -deslocamento / LIMITE_ARRASTE);
  });

  const soltar = () => {
    if (inicioX === null) return;
    inicioX = null;
    seloGostei.style.opacity = 0;
    seloPassar.style.opacity = 0;
    if (deslocamento > LIMITE_ARRASTE) {
      votar('GOSTEI');
    } else if (deslocamento < -LIMITE_ARRASTE) {
      votar('PASSAR');
    } else {
      card.classList.remove('arrastando'); // volta para o lugar
      card.style.transform = '';
    }
    deslocamento = 0;
  };
  card.addEventListener('pointerup', soltar);
  card.addEventListener('pointercancel', soltar);
}

// ---------- Comentários (janela por cima) ----------
async function abrirComentarios(exp) {
  if (!exp) return;
  const fundo = document.createElement('div');
  fundo.className = 'modal-fundo';
  fundo.innerHTML = `
    <div class="modal" role="dialog" aria-label="Comentários">
      <div class="secao-cabecalho"><h2>💬 Comentários</h2><button class="botao-link" id="fechar">Fechar</button></div>
      <p class="suave pequeno" style="margin-bottom:10px">${esc(exp.livro.titulo)} · por ${esc(exp.usuario.nome)}</p>
      <div id="lista-comentarios"></div>
      <div class="campo" style="margin-top:16px">
        <textarea id="novo-comentario" placeholder="Escreva um comentário..." maxlength="1000" style="min-height:80px"></textarea>
      </div>
      <button class="botao botao-largo" id="enviar">Comentar</button>
    </div>`;
  document.body.appendChild(fundo);

  const fechar = () => fundo.remove();
  fundo.querySelector('#fechar').addEventListener('click', fechar);
  fundo.addEventListener('click', e => { if (e.target === fundo) fechar(); });

  const lista = fundo.querySelector('#lista-comentarios');
  const mostrar = async () => {
    await carregar(lista, async () => {
      const comentarios = await api(`/experiencias/${exp.id}/comentarios`);
      lista.innerHTML = comentarios.length
        ? comentarios.map(c => `
            <div class="comentario">
              <span class="avatar">${esc(iniciais(c.usuario.nome))}</span>
              <div><strong>${esc(c.usuario.nome)}</strong><br>${esc(c.texto)}</div>
            </div>`).join('')
        : '<p class="suave">Ninguém comentou ainda. Seja o primeiro!</p>';
    });
  };
  await mostrar();

  fundo.querySelector('#enviar').addEventListener('click', async () => {
    const campo = fundo.querySelector('#novo-comentario');
    try {
      await api(`/experiencias/${exp.id}/comentarios`, 'POST', { texto: campo.value });
      campo.value = '';
      exp.totalComentarios++;
      await mostrar();
    } catch (e) {
      aviso(e.message, 'erro');
    }
  });
}
