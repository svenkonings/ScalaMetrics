import argparse

import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import StratifiedKFold, cross_val_predict

from analysis import categories, projects, save_dataframe, get_metric_results, to_binary, get_columns, get_stats


def main(args):
    folder = f'{args.folder}/regression/multivariate/'
    estimator = LogisticRegression(class_weight='balanced', random_state=42)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    for category in categories:
        multivariate_regression_results = pd.DataFrame(
            columns=['name', 'tn', 'fp', 'fn', 'tp', 'r2', 'precision', 'recall', 'mcc']
        )
        for path, name in projects.items():
            print(f'[{category}] Multivariate: {name}')
            df = get_metric_results(args.folder, path, category)
            columns = get_columns(df)
            faults = df['faults'].apply(to_binary)
            data = df[columns]
            prediction = cross_val_predict(estimator, data, faults, cv=cv)
            result = get_stats(faults, prediction)
            result['name'] = name
            multivariate_regression_results = multivariate_regression_results.append(result, ignore_index=True)
        save_dataframe(multivariate_regression_results, folder, category, False)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--folder', help='Select folder to analyse', dest='folder', required=True)
    main(parser.parse_args())
