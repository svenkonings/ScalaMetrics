from glob import glob

import pandas as pd

from analysis import save_dataframe, categories, parse_args


def main(args):
    folder = f'{args.folder}/regression/univariate/'
    for category in categories:
        if args.split_paradigm_score:
            for paradigm in ['Neutral', 'OOP', 'FP', 'Mix']:
                path = folder + category + paradigm
        else:
            path = folder + category
        df = read_all(path, ['name', 'precision', 'recall', 'mcc'])
        if df is not None:
            summarise(df, path, category)


def summarise(df, path, category):
    print(f'Summarise {category}')
    means = df.groupby('name').agg(['mean', 'std'])
    means.columns = means.columns.map(' '.join)
    save_dataframe(means, path, 'means')
    medians = df.groupby('name').median()
    save_dataframe(medians, path, 'medians')


def read_all(path, columns):
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
