import argparse

import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import StratifiedKFold, cross_val_predict

from analysis import categories, projects, save_dataframe, get_metric_results, get_columns, to_binary, get_stats


def main(args):
    folder = f'{args.folder}/regression/univariate/'
    estimator = LogisticRegression(class_weight='balanced', random_state=42)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    for category in categories:
        for path, name in projects.items():
            print(f'[{category}] Univariate: {name}')
            df = get_metric_results(args.folder, path, category)
            columns = get_columns(df)
            faults = df['faults'].apply(to_binary)
            result = pd.DataFrame(
                columns=['name', 'tn', 'fp', 'fn', 'tp', 'r2', 'precision', 'recall', 'mcc']
            )
            for column in columns:
                print(f'[{category}] {name}: {column}')
                data = df[column].values.reshape(-1, 1)
                prediction = cross_val_predict(estimator, data, faults, cv=cv)
                column_result = get_stats(faults, prediction)
                column_result['name'] = column
                result = result.append(column_result, ignore_index=True)
            save_dataframe(result, folder + category, name, False)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--folder', help='Select folder to analyse', dest='folder', required=True)
    main(parser.parse_args())
