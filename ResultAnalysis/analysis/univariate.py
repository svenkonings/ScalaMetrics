import warnings

import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import StratifiedKFold, cross_val_predict

from analysis import categories, projects, save_dataframe, get_metric_results, get_columns, to_binary, get_stats, \
    parse_args, split_paradigm_score


def main(args):
    warnings.filterwarnings("ignore", category=RuntimeWarning, module="sklearn")
    folder = f'{args.folder}/regression/univariate/'
    estimator = LogisticRegression(class_weight='balanced', random_state=42)
    cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)
    for category in categories:
        for path, name in projects.items():
            df = get_metric_results(args.folder, path, category)
            if df is not None:
                if args.split_paradigm_score:
                    for paradigm, scores in split_paradigm_score(df):
                        univariate(scores, folder, category + paradigm, name, estimator, cv, args)
                else:
                    univariate(df, folder, category, name, estimator, cv, args)


def univariate(df, folder, category, name, estimator, cv, args):
    print(f'[{category}] Univariate: {name}')
    if len(df) < 50:
        print('Less than 50 entries -- skipping!')
        return
    columns = get_columns(df, args)
    faults = df['faults'].apply(to_binary)
    if faults.min() == faults.max():
        print('Single category -- skipping!')
        return
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
    main(parse_args())
