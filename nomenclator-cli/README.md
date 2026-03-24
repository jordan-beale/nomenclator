# Nomenclator CLI

## Default titles list

The default canonical titles list is:
- Architect
- Software engineer
- Quantity surveyor
- Accountant

### General usage

```text
Usage: nomenclator-cli [-hvV] [-z=<snoozeTime>] [COMMAND]
Nomenclator command line interface
  -h, --help           Show this help message and exit.
  -v, --[no-]verbose   Verbose output
                         Default: false
  -V, --version        Print version information and exit.
  -z, --snooze=<snoozeTime>
                       Snooze seconds before startup
Commands:
  help            Display help information about the specified command.
  normalise       Normalise a title against a list of normalised titles
  normaliseBatch  Normalise a file of titles against a list of normalised titles
```

### Normalise using default titles list

```shell
docker run --rm nomenclator-cli:latest normalise "Java developer"
```

Output
```shell
Normalising job title [Java developer]...
Normalised job title [Java developer] to [Software engineer].
```

### Normalise using custom titles list

```shell
docker run --rm \
  -v ~/nomenclator/nomenclator-cli/src/test/resources/csv:/data \
  nomenclator-cli:latest \
  normalise --titles /data/multi_line_no_header.csv "customer marketing team lead"
```

Output
```shell
Normalising job title [customer marketing team lead]...
Normalised job title [customer marketing team lead] to [Customer Success Manager].
```

### NormaliseBatch using default titles list

```shell
docker run --rm \
  -v ~/nomenclator/nomenclator-cli/src/test/resources/csv:/data \
  nomenclator-cli:latest \
  normaliseBatch --input /data/toNormalise.csv --output /data/normalised.csv
```

Output
```shell
Normalising job titles from file [/data/toNormalise.csv]...
Normalised job titles from [/data/toNormalise.csv] to [/data/normalised.csv].
```

### NormaliseBatch using custom titles list

```shell
docker run --rm \
  -v ~/nomenclator/nomenclator-cli/src/test/resources/csv:/data \
  nomenclator-cli:latest \
  normaliseBatch --input /data/toNormalise.csv --output /data/normalised.csv --titles /data/multi_line.csv
```

Output
```shell
Normalising job titles from file [/data/toNormalise.csv]...
Normalised job titles from [/data/toNormalise.csv] to [/data/normalised.csv].
```

## CSV format

The `--titles` and `--input` options accept CSV files in any of the following formats:

**Single row:**
```
Software engineer,Accountant,Architect,Quantity surveyor
```

**Multi-line without header:**
```
Software engineer
Accountant
Architect
```

**Multi-line with header:**
```
title
Software engineer
Accountant
Architect
```

The `--ouput` gets written as a multi-line without header.
