// Publicar experiência: escolher livro, nota em estrelas, sentimento, texto e trecho

const usuario = montarLayout('');
const SENTIMENTOS = ['Empolgado', 'Emocionado', 'Reflexivo', 'Nostálgico', 'Assustado', 'Divertido', 'Inspirado'];

let livroEscolhido = null;
let nota = 0;
let sentimento = null;

// ----- Livro: vem pela URL (publicar.html?livro=3) ou pela busca -----
const idDaUrl = parametro('livro');
if (idDaUrl) {
  api('/livros/' + idDaUrl).then(escolherLivro).catch(e => aviso(e.message, 'erro'));
}

const busca = document.getElementById('busca-livro');
const opcoes = document.getElementById('opcoes-livro');
let espera;
busca.addEventListener('input', () => {
  clearTimeout(espera);
  espera = setTimeout(async () => {
    const texto = busca.value.trim();
    if (texto.length < 2) { opcoes.innerHTML = ''; return; }
    const livros = await api('/livros?busca=' + encodeURIComponent(texto));
    opcoes.innerHTML = livros.length
      ? livros.map(l => `<div class="busca-item" data-id="${l.id}" style="cursor:pointer">${capa(l, 'mini')}<span><strong>${esc(l.titulo)}</strong><br><small>${esc(l.autor)}</small></span></div>`).join('')
      : '<p class="suave pequeno">Nenhum livro encontrado.</p>';
    opcoes.querySelectorAll('[data-id]').forEach(item => item.addEventListener('click', () => {
      escolherLivro(livros.find(l => l.id === Number(item.dataset.id)));
    }));
  }, 300);
});

// Livro que ainda não existe no BookSwipe: busca na API pública e já escolhe o importado
document.getElementById('buscar-internet').addEventListener('click', () => {
  abrirBuscaNaInternet(busca.value.trim(), escolherLivro);
});

function escolherLivro(livro) {
  livroEscolhido = livro;
  document.getElementById('escolher-livro').hidden = true;
  const caixa = document.getElementById('livro-escolhido');
  caixa.hidden = false;
  caixa.innerHTML = `${capa(livro, 'mini')}
    <div style="flex:1"><strong>${esc(livro.titulo)}</strong><br><span class="suave pequeno">${esc(livro.autor)}</span></div>
    <button type="button" class="botao-link" id="trocar-livro">Trocar</button>`;
  document.getElementById('trocar-livro').addEventListener('click', () => {
    livroEscolhido = null;
    caixa.hidden = true;
    document.getElementById('escolher-livro').hidden = false;
    busca.focus();
  });
}

// ----- Nota: 5 estrelas clicáveis -----
const caixaNota = document.getElementById('nota');
caixaNota.innerHTML = [1, 2, 3, 4, 5]
  .map(n => `<button type="button" data-nota="${n}" aria-label="${n} estrela(s)">★</button>`).join('');
caixaNota.querySelectorAll('button').forEach(botao => botao.addEventListener('click', () => {
  nota = Number(botao.dataset.nota);
  caixaNota.querySelectorAll('button').forEach(b => b.classList.toggle('acesa', Number(b.dataset.nota) <= nota));
}));

// ----- Sentimento: um só -----
const caixaSentimentos = document.getElementById('sentimentos');
caixaSentimentos.innerHTML = SENTIMENTOS
  .map(s => `<button type="button" class="chip-botao" data-sentimento="${s}">${s}</button>`).join('');
caixaSentimentos.querySelectorAll('button').forEach(botao => botao.addEventListener('click', () => {
  const jaEra = botao.classList.contains('ativo');
  caixaSentimentos.querySelectorAll('button').forEach(b => b.classList.remove('ativo'));
  sentimento = jaEra ? null : botao.dataset.sentimento;
  if (!jaEra) botao.classList.add('ativo');
}));

// ----- Contador de caracteres -----
const texto = document.getElementById('texto');
texto.addEventListener('input', () => {
  document.getElementById('contador-texto').textContent = `${texto.value.length}/500`;
});

// ----- Publicar -----
document.getElementById('form').addEventListener('submit', async evento => {
  evento.preventDefault();
  const erro = document.getElementById('erro');
  erro.textContent = '';
  if (!livroEscolhido) { erro.textContent = 'Escolha o livro.'; return; }

  try {
    await api('/experiencias', 'POST', {
      livroId: livroEscolhido.id,
      nota,
      sentimento,
      texto: texto.value.trim(),
      trechoFavorito: document.getElementById('trecho').value.trim() || null,
    });
    aviso('Experiência publicada! 🎉');
    setTimeout(() => location.href = `livro.html?id=${livroEscolhido.id}`, 900);
  } catch (e) {
    erro.textContent = e.message; // ex: "Você já publicou uma experiência sobre este livro"
  }
});
