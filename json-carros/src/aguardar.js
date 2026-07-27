function aguardar(milissegundos) {
  return new Promise((resolve) => setTimeout(resolve, milissegundos));
}

module.exports = { aguardar };
