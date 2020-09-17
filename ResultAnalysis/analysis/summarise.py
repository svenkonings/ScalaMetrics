from glob import glob

import pandas as pd

from analysis import save_dataframe, categories, parse_args


def main(args):
    if args.split_paradigm_score:
        folder = f'{args.folder}/split-regression/univariate/'
    else:
        folder = f'{args.folder}/regression/univariate/'
    for category in categories:
        path = folder + category
        if args.split_paradigm_score:
            for paradigm in ['Neutral', 'OOP', 'FP', 'Mix']:
                df = read_all(path, ['name', 'precision', 'recall', 'mcc'], paradigm)
                if df is not None:
                    summarise(df, path, category + paradigm, paradigm)
        else:
            df = read_all(path, ['name', 'precision', 'recall', 'mcc'])
            if df is not None:
                summarise(df, path, category)


def summarise(df, path, category, suffix=''):
    print(f'Summarise {category}')
    means = df.groupby('name').agg(['mean', 'std'])
    means.columns = means.columns.map(' '.join)
    save_dataframe(means, path, 'means' + suffix)
    medians = df.groupby('name').median()
    save_dataframe(medians, path, 'medians' + suffix)


def read_all(path, columns, paradigm=None):
    if paradigm:
        files = glob('../data/analysisResults/' + path + '/[!m]*' + paradigm + '.csv')
    else:
        files = glob('../data/analysisResults/' + path + '/[!m]*.csv')
    if files:
        all_df = pd.DataFrame(columns=columns)
        for file in files:
            df = pd.read_csv(file)[columns]
            all_df = all_df.append(df, ignore_index=True)
        return all_df
    else:
        return None


if __name__ == '__main__':
    main(parse_args())
