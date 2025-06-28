# Relatório Final: Sistema de Gerenciamento de Filmes com Estruturas de Dados Avançadas

## Resumo
Este relatório documenta o desenvolvimento de um sistema completo de gerenciamento de filmes implementado em Java durante a disciplina Algoritmos e Estruturas de Dados III (AEDS3) do 3º período do curso de Ciência da Computação da PUC Minas. O projeto foi dividido em quatro trabalhos práticos (TP1-TP4) que implementaram diferentes aspectos de manipulação de dados: organização de arquivos binários, estruturas de indexação, compressão/descompressão de dados, casamento de padrões e criptografia. O sistema processa um conjunto de dados CSV de filmes e os armazena em formato binário utilizando diversas estruturas de dados para otimizar operações CRUD.

## Introdução
O armazenamento e recuperação eficiente de grandes volumes de dados constitui um desafio fundamental na ciência da computação. O projeto desenvolvido aborda esta problemática através da implementação de um sistema que processa dados de filmes contendo os atributos: show_id, type, title, director, cast, country, date_added, release_year, rating, duration, listed_in e description.
O objetivo principal foi implementar e comparar diferentes estruturas de dados para armazenamento em arquivo binário, incluindo organização sequencial, árvore B+, tabela hash e lista invertida. Além disso, foram implementados algoritmos de compressão, casamento de padrões e criptografia para demonstrar técnicas avançadas de manipulação de dados.

## Desenvolvimento

### TP1: Arquivo Sequencial e Ordenação Externa
O primeiro trabalho prático focou na implementação da manipulação de dados em arquivo sequencial binário. A estrutura sequencial foi escolhida como base do sistema devido à sua simplicidade de implementação e eficiência no acesso a grandes volumes de dados.
Implementação do CRUD: O sistema implementa operações completas de Create, Read, Update e Delete para os registros de filmes. Os dados são armazenados sequencialmente no arquivo binário, com cada registro precedido por um marcador de validade e informações de tamanho.
Ordenação em Memória Secundária: Foi implementado o algoritmo de intercalação balanceada para ordenação dos registros diretamente no arquivo. Esta técnica é fundamental quando o volume de dados excede a capacidade da memória principal, permitindo ordenar arquivos de qualquer tamanho através da divisão em blocos menores que são ordenados em memória e posteriormente intercalados.

### TP2: Estruturas de Indexação Avançadas
O segundo trabalho prático expandiu o sistema com a implementação de três estruturas de indexação: hash table, árvore B+ e lista invertida.
Tabela Hash: A implementação utiliza hash externo para permitir o armazenamento de grandes volumes de dados em disco. A função hash distribui uniformemente os registros pelos buckets, garantindo tempo médio O(1) para operações de busca.
Árvore B+: Esta estrutura foi escolhida por sua eficiência em operações de busca, inserção e remoção em memória secundária. A árvore B+ mantém todos os dados nas folhas e garante que a árvore permaneça balanceada, proporcionando tempo de acesso logarítmico. Os nós internos contêm apenas chaves de navegação, otimizando o uso de memória e reduzindo o número de acessos ao disco.
Lista Invertida: Implementada para permitir busca eficiente por conteúdo textual. Esta estrutura mantém um índice de termos onde cada termo aponta para uma lista de registros que o contêm. É particularmente útil para buscas por diretor, elenco ou descrição dos filmes.

### TP3: Compressão e Casamento de Padrões
O terceiro trabalho prático implementou algoritmos de compressão de dados e casamento de padrões.
Compressão LZW: O algoritmo Lempel-Ziv-Welch foi implementado para compressão sem perdas dos dados. O LZW constrói dinamicamente um dicionário de sequências de dados repetidas, substituindo-as por códigos mais curtos. Este método é particularmente eficaz para dados que contêm muitas repetições.
Compressão Huffman: O algoritmo de Huffman implementa compressão estatística baseada na frequência de caracteres. Caracteres mais frequentes recebem códigos binários mais curtos, reduzindo significativamente o tamanho dos arquivos de texto. A técnica é amplamente utilizada em formatos de compressão modernos.
Casamento de Padrões Boyer-Moore: Este algoritmo foi implementado para busca eficiente de padrões nos textos. O Boyer-Moore compara caracteres da direita para a esquerda e utiliza duas heurísticas (caractere ruim e sufixo bom) para determinar saltos ótimos durante a busca.
Casamento de Padrões KMP: O algoritmo Knuth-Morris-Pratt complementa o Boyer-Moore, utilizando uma função de falha para evitar comparações desnecessárias3. Ambos os algoritmos garantem busca de padrões em tempo linear.

### TP4: Criptografia
O quarto trabalho prático implementou técnicas de criptografia para proteção dos dados.
Cifra de César: Implementada como exemplo de criptografia clássica por substituição. Apesar de sua simplicidade, demonstra os princípios fundamentais da criptografia simétrica. A cifra foi aplicada especificamente aos dados do arquivo sequencial.
Criptografia RSA: Sistema de criptografia de chave pública implementado para demonstrar técnicas modernas de segurança. O RSA utiliza a dificuldade computacional de fatoração de números primos grandes para garantir a segurança. Esta implementação também foi restrita ao arquivo sequencial para manter a consistência com a arquitetura do sistema.

## Testes e Resultados
Todos os componentes do sistema foram extensivamente testados e funcionam de maneira perfeita. Os testes incluíram:
Validação do CRUD: Todas as operações de criação, leitura, atualização e exclusão foram testadas com conjuntos de dados de diferentes tamanhos, confirmando a integridade dos dados em todas as estruturas implementadas.
Performance das Estruturas: Os testes de performance demonstraram as vantagens específicas de cada estrutura: a árvore B+ mostrou excelente performance para buscas ordenadas, a tabela hash ofereceu acesso constante para buscas por chave, e a lista invertida permitiu buscas textuais eficientes.
Eficiência da Compressão: Os algoritmos LZW e Huffman demonstraram taxas de compressão significativas, com o LZW sendo mais eficaz para dados com repetições e o Huffman para texto com distribuição desigual de caracteres.
Precisão do Casamento de Padrões: Os algoritmos Boyer-Moore e KMP foram testados com diversos padrões e textos, confirmando 100% de precisão na localização de padrões.
Segurança Criptográfica: Os sistemas de criptografia César e RSA foram validados através de ciclos de encriptação e decriptação, confirmando a preservação integral dos dados.

## Conclusão
O desenvolvimento deste sistema demonstrou com sucesso a aplicação prática de estruturas de dados avançadas em um contexto real de gerenciamento de informações. Cada estrutura implementada atende a necessidades específicas: arquivos sequenciais para armazenamento básico e ordenação, árvores B+ para indexação eficiente, hash tables para acesso rápido por chave, e listas invertidas para busca textual.
A escolha dos algoritmos foi fundamentada em suas características específicas e aplicabilidade ao domínio do problema. O LZW e Huffman foram selecionados por serem algoritmos clássicos de compressão com aplicações bem estabelecidas. Os algoritmos de casamento de padrões Boyer-Moore e KMP representam o estado da arte em busca de texto. As técnicas criptográficas implementadas demonstram tanto conceitos históricos (César) quanto modernos (RSA) de segurança da informação.
A restrição da ordenação e criptografia ao arquivo sequencial foi uma decisão arquitetural que manteve a coerência do sistema e demonstrou a especialização de diferentes estruturas para diferentes propósitos. O arquivo sequencial, sendo a estrutura mais fundamental, serviu como base para demonstrar essas técnicas adicionais.
Este projeto, desenvolvido por Gabriel Diniz Reis Vianna e Arthur Costa Serra Negra durante o 3º período de Ciência da Computação da PUC Minas, consolidou conhecimentos essenciais em algoritmos e estruturas de dados, proporcionando experiência prática com técnicas que são fundamentais na engenharia de software moderna.
O sistema completo demonstra a importância da escolha adequada de estruturas de dados e algoritmos baseada nos requisitos específicos de cada aplicação, confirmando que diferentes problemas demandam diferentes soluções otimizadas.


