// Cadastro em 2 passos: dados -> gêneros favoritos

document.getElementById('logo').innerHTML = LOGO_SVG + '<span>BookSwipe</span>';

const passoDados = document.getElementById('passo-dados');
const passoGeneros = document.getElementById('passo-generos');
const escolhidos = new Set(); // ids dos gêneros marcados

// ----- Passo 1: cria a conta -----
passoDados.addEventListener('submit', async evento => {
  evento.preventDefault();
  const erro = document.getElementById('erro-dados');
  erro.textContent = '';

  try {
    const resposta = await api('/usuarios', 'POST', {
      nome: document.getElementById('nome').value.trim(),
      email: document.getElementById('email').value.trim(),
      senha: document.getElementById('senha').value,
      curso: document.getElementById('curso').value.trim(),
      bairro: document.getElementById('bairro').value.trim(),
    });
    sessao.salvar(resposta); // já fica logado (guarda o token)
    await mostrarGeneros();
  } catch (e) {
    erro.textContent = e.message; // ex: "Já existe uma conta com este e-mail"
  }
});

// ----- Passo 2: escolhe os gêneros -----
async function mostrarGeneros() {
  const generos = await api('/generos');
  const caixa = document.getElementById('generos');
  caixa.innerHTML = generos
    .map(g => `<button type="button" class="chip-botao" data-id="${g.id}">${esc(g.nome)}</button>`).join('');

  caixa.querySelectorAll('.chip-botao').forEach(botao => {
    botao.addEventListener('click', () => {
      const id = Number(botao.dataset.id);
      botao.classList.toggle('ativo');
      if (escolhidos.has(id)) escolhidos.delete(id); else escolhidos.add(id);
    });
  });

  passoDados.hidden = true;
  passoGeneros.hidden = false;
}

document.getElementById('salvar-generos').addEventListener('click', async () => {
  const erro = document.getElementById('erro-generos');
  try {
    await api(`/usuarios/${sessao.usuario().id}/generos`, 'PUT', { generoIds: [...escolhidos] });
    location.href = 'inicio.html';
  } catch (e) {
    erro.textContent = e.message; // ex: "Escolha pelo menos um gênero"
  }
});
