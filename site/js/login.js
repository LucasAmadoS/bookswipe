// Tela de login

document.getElementById('logo').innerHTML = LOGO_SVG + '<span>BookSwipe</span>';
desenharEstante();

const form = document.getElementById('form-login');
const erro = document.getElementById('erro');

form.addEventListener('submit', async evento => {
  evento.preventDefault(); // não recarrega a página: quem envia é o JavaScript
  erro.textContent = '';
  const botao = form.querySelector('button');
  botao.disabled = true;

  try {
    const resposta = await api('/login', 'POST', {
      email: document.getElementById('email').value.trim(),
      senha: document.getElementById('senha').value,
    });
    sessao.salvar(resposta); // guarda o token + o usuário
    location.href = 'inicio.html';
  } catch (e) {
    erro.textContent = e.message; // ex: "E-mail ou senha incorretos" (vem da API)
    botao.disabled = false;
  }
});

// Lombadas de livros coloridas no lado direito (só enfeite)
function desenharEstante() {
  const cores = ['#E67E3E', '#4CAF7D', '#F7F5F0', '#7A2E2E', '#E8B43C', '#2B4C7E', '#4CAF7D', '#E67E3E', '#F7F5F0', '#8A5A1E'];
  const alturas = [150, 180, 130, 170, 120, 190, 140, 165, 125, 175];
  document.getElementById('estante-arte').innerHTML = cores
    .map((cor, i) => `<span style="background:${cor};height:${alturas[i]}px"></span>`).join('');
}
